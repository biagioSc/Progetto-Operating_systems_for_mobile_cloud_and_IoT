package com.example.robotinteraction;


import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.example.robotinteraction.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestAutomatico1 {

    @Rule
    public ActivityScenarioRule<Activity1_New> mActivityScenarioRule =
            new ActivityScenarioRule<>(Activity1_New.class);

    @Test
    public void testAutomatico1() {
        ViewInteraction textInputEditText = onView(
allOf(withId(R.id.editTextEmail),
childAtPosition(
childAtPosition(
withId(R.id.emailTextInputLayout),
1),
0),
isDisplayed()));
        textInputEditText.perform(replaceText("biagio@gmail.com"), closeSoftKeyboard());

        ViewInteraction button = onView(
allOf(withId(R.id.buttonWaitingRoom), withText("Riprendi"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
3),
isDisplayed()));
        button.perform(click());

        ViewInteraction textInputEditText2 = onView(
allOf(withId(R.id.editTextPassword),
childAtPosition(
childAtPosition(
withId(R.id.passwordTextInputLayout),
1),
0),
isDisplayed()));
        textInputEditText2.perform(replaceText("password"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton = onView(
allOf(withId(R.id.passwordToggle), withContentDescription("Toggle Password Visibility"),
childAtPosition(
childAtPosition(
withId(R.id.passwordTextInputLayout),
1),
1),
isDisplayed()));
        appCompatImageButton.perform(click());

        pressBack();

        ViewInteraction materialButton = onView(
allOf(withId(R.id.buttonLogin), withText("Accedi"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
6),
isDisplayed()));
        materialButton.perform(click());

        ViewInteraction button2 = onView(
allOf(withId(R.id.buttonChecknextState), withText("Accedi alla Coda"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
5),
isDisplayed()));
        button2.perform(click());

        ViewInteraction appCompatSpinner = onView(
allOf(withId(R.id.spinnerDrinks),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
6),
isDisplayed()));
        appCompatSpinner.perform(click());

        DataInteraction appCompatCheckedTextView = onData(anything())
.inAdapterView(childAtPosition(
withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
0))
.atPosition(4);
        appCompatCheckedTextView.perform(click());

        ViewInteraction materialButton2 = onView(
allOf(withId(R.id.buttonOrder), withText("Ordina"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
8),
isDisplayed()));
        materialButton2.perform(click());

        ViewInteraction materialButton3 = onView(
allOf(withId(R.id.buttonWaitingRoom), withText("Sala d'Attesa"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
4),
isDisplayed()));
        materialButton3.perform(click());

        ViewInteraction materialButton4 = onView(
allOf(withId(R.id.buttonExit), withText("Esci"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
6),
isDisplayed()));
        materialButton4.perform(click());
        }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
    }
