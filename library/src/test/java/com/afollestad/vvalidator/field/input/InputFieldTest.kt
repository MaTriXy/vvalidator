/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.vvalidator.field.input

import android.widget.EditText
import com.afollestad.vvalidator.assertion.CustomViewAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.ContainsAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.EmailAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.LengthAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.NotEmptyAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.NumberAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.RegexAssertion
import com.afollestad.vvalidator.assertion.input.InputAssertions.UriAssertion
import com.afollestad.vvalidator.field.FieldError
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.Form
import com.afollestad.vvalidator.testutil.ID_INPUT
import com.afollestad.vvalidator.testutil.NoManifestTestRunner
import com.afollestad.vvalidator.testutil.TestActivity
import com.afollestad.vvalidator.testutil.assertEmpty
import com.afollestad.vvalidator.testutil.assertEqualTo
import com.afollestad.vvalidator.testutil.assertFalse
import com.afollestad.vvalidator.testutil.assertNotNull
import com.afollestad.vvalidator.testutil.assertNull
import com.afollestad.vvalidator.testutil.assertSize
import com.afollestad.vvalidator.testutil.assertTrue
import com.afollestad.vvalidator.testutil.assertType
import com.afollestad.vvalidator.testutil.triggerTextChanged
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

/** @author Aidan Follestad (@afollestad) */
@RunWith(NoManifestTestRunner::class)
class InputFieldTest {

  private lateinit var activity: ActivityController<TestActivity>
  private lateinit var form: Form
  private lateinit var field: InputField

  @Before fun setup() {
    activity = Robolectric.buildActivity(TestActivity::class.java)
        .apply {
          create()
        }
    form = activity.get()
        .form {
          input(ID_INPUT, name = "Input") {}
        }
    field = form.getFields()
        .single()
        .assertType()
    field.view.assertEqualTo(activity.get().input)
  }

  @Test fun isNotEmpty() {
    val assertion = field.isNotEmpty()
        .assertType<NotEmptyAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun isUrl() {
    val assertion = field.isUrl()
        .assertType<UriAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()

    field.view.setText("https://af.codes")
    assertion.isValid(field.view)
        .assertTrue()

    field.view.setText("https://af.codes/test.html")
    assertion.isValid(field.view)
        .assertTrue()

    field.view.setText("http://www.af.codes?q=hello+world")
    assertion.isValid(field.view)
        .assertTrue()

    field.view.setText("https://")
    assertion.isValid(field.view)
        .assertFalse()

    field.view.setText("https://?q=hello")
    assertion.isValid(field.view)
        .assertFalse()

    field.view.setText("ftp://af.codes")
    assertion.isValid(field.view)
        .assertFalse()
  }

  @Test fun isUri() {
    val assertion = field.isUri()
        .assertType<UriAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun isEmail() {
    val assertion = field.isEmail()
        .assertType<EmailAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun isNumber() {
    val assertion = field.isNumber()
        .assertType<NumberAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun length() {
    val assertion = field.length()
        .assertType<LengthAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun contains() {
    val assertion = field.contains("hello")
        .assertType<ContainsAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun matches() {
    val assertion = field.matches("hello|world")
        .assertType<RegexAssertion>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun assert_custom() {
    val assertion = field.assert("test") { true }
        .assertType<CustomViewAssertion<EditText>>()
    field.assertions()
        .single()
        .assertEqualTo(assertion)
    assertion.conditions.assertEmpty()
  }

  @Test fun isEmptyOr() {
    field.isEmptyOr { isUrl() }
    val assertion = field.assertions()
        .single()
        .assertType<UriAssertion>()
    assertion.conditions.assertNotNull()
        .assertSize(1)
  }

  @Test fun onErrors() {
    val errors = listOf(
        FieldError(
            id = ID_INPUT,
            name = "Input",
            description = "must not be empty",
            assertionType = NotEmptyAssertion::class
        )
    )
    field.propagateErrors(false, errors)
    field.view.error.assertEqualTo("must not be empty")
  }

  @Test fun `real time validation off`() {
    field.isNotEmpty()

    field.view.triggerTextChanged("hello")
    field.view.error.assertNull()

    field.view.triggerTextChanged("")
    field.view.error.assertNull()
  }

  @Test fun `real time validation on`() {
    field.startRealTimeValidation(0)
    field.isNotEmpty()

    field.view.triggerTextChanged("hello")
    field.view.error.assertNull()

    field.view.triggerTextChanged("")
    field.view.error.assertEqualTo("cannot be empty")
  }
}
