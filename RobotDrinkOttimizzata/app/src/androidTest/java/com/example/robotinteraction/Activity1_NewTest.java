package com.example.robotinteraction;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Activity1_NewTest {

    @Rule
    public ActivityScenarioRule<Activity1_New> mActivityScenarioRule =
            new ActivityScenarioRule<>(Activity1_New.class);

    @Test
    public void activity1_NewTest() {
        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.editTextEmail),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.emailTextInputLayout),
                                        1),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("biagio@gmail.com"), closeSoftKeyboard());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.editTextPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.passwordTextInputLayout),
                                        1),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("password"), closeSoftKeyboard());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.buttonLogin), withText("Accedi"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                6),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.buttonChecknextState), withText("Accedi alla Coda"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                5),
                        isDisplayed()));
        button.perform(click());

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
                .atPosition(5);
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
                allOf(withId(R.id.buttonQuiz), withText("Quiz"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                5),
                        isDisplayed()));
        materialButton3.perform(click());

        ViewInteraction materialRadioButton = onView(
                allOf(withId(R.id.answerOptionA), withText("A) Povert�"),
                        childAtPosition(
                                allOf(withId(R.id.answerRadioGroup),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        materialRadioButton.perform(click());

        ViewInteraction materialButton4 = onView(
                allOf(withId(R.id.confirmButton), withText("Conferma Risposta"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        2),
                                2),
                        isDisplayed()));
        materialButton4.perform(click());

        ViewInteraction materialRadioButton2 = onView(
                allOf(withId(R.id.answerOptionC), withText("C) Barack Obama"),
                        childAtPosition(
                                allOf(withId(R.id.answerRadioGroup),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        materialRadioButton2.perform(click());

        ViewInteraction materialButton5 = onView(
                allOf(withId(R.id.confirmButton), withText("Conferma Risposta"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        2),
                                2),
                        isDisplayed()));
        materialButton5.perform(click());

        ViewInteraction materialRadioButton3 = onView(
                allOf(withId(R.id.answerOptionB), withText("B) Euro Bit"),
                        childAtPosition(
                                allOf(withId(R.id.answerRadioGroup),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        materialRadioButton3.perform(click());

        ViewInteraction materialButton6 = onView(
                allOf(withId(R.id.confirmButton), withText("Conferma Risposta"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        2),
                                2),
                        isDisplayed()));
        materialButton6.perform(click());

        ViewInteraction materialRadioButton4 = onView(
                allOf(withId(R.id.answerOptionB), withText("B) La legge dell'attrito"),
                        childAtPosition(
                                allOf(withId(R.id.answerRadioGroup),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        materialRadioButton4.perform(click());

        ViewInteraction materialButton7 = onView(
                allOf(withId(R.id.confirmButton), withText("Conferma Risposta"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        2),
                                2),
                        isDisplayed()));
        materialButton7.perform(click());

        ViewInteraction materialRadioButton5 = onView(
                allOf(withId(R.id.answerOptionC), withText("C) Ossigeno"),
                        childAtPosition(
                                allOf(withId(R.id.answerRadioGroup),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        materialRadioButton5.perform(click());

        ViewInteraction materialButton8 = onView(
                allOf(withId(R.id.confirmButton), withText("Conferma Risposta"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        2),
                                2),
                        isDisplayed()));
        materialButton8.perform(click());

        ViewInteraction materialRadioButton6 = onView(
                allOf(withId(R.id.answerOptionB), withText("B) Ampere"),
                        childAtPosition(
                                allOf(withId(R.id.answerRadioGroup),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        materialRadioButton6.perform(click());

        ViewInteraction materialButton9 = onView(
                allOf(withId(R.id.confirmButton), withText("Conferma Risposta"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        2),
                                2),
                        isDisplayed()));
        materialButton9.perform(click());

        ViewInteraction materialButton10 = onView(
                allOf(withId(android.R.id.button1), withText("Ok"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        materialButton10.perform(scrollTo(), click());

        ViewInteraction materialButton11 = onView(
                allOf(withId(R.id.buttonRetrieve), withText("Ritira"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                6),
                        isDisplayed()));
        materialButton11.perform(click());

        ViewInteraction materialButton12 = onView(
                allOf(withId(R.id.buttonExit), withText("Esci"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                6),
                        isDisplayed()));
        materialButton12.perform(click());
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
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
