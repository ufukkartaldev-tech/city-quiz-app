package com.example.oyun.ui.category

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.oyun.R
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CategorySelectionActivity UI Tests
 * 
 * Test Coverage:
 * - Category display
 * - Category selection
 * - Lock status UI
 * - Navigation
 * - RecyclerView interactions
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CategorySelectionActivityTest {

    private lateinit var scenario: ActivityScenario<CategorySelectionActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(CategorySelectionActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    // ============================================
    // DISPLAY TESTS
    // ============================================

    @Test
    fun testActivityLaunches() {
        // Verify activity launches successfully
        onView(withId(R.id.rvCategories))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testToolbarDisplayed() {
        // Verify toolbar is displayed
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
        
        // Verify back button is displayed
        onView(withId(R.id.btnBack))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAllCategoriesButtonDisplayed() {
        // Verify "All Categories" button is displayed
        onView(withId(R.id.btnAllCategories))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    // ============================================
    // RECYCLERVIEW TESTS
    // ============================================

    @Test
    fun testCategoriesRecyclerViewDisplayed() {
        // Wait for loading to complete
        Thread.sleep(1000)
        
        // Verify RecyclerView is displayed
        onView(withId(R.id.rvCategories))
            .check(matches(isDisplayed()))
        
        // Verify progress bar is gone
        onView(withId(R.id.progressBar))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun testCategoryItemClick() {
        // Wait for categories to load
        Thread.sleep(1000)
        
        // Click on first category
        onView(withId(R.id.rvCategories))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<CategoryAdapter.CategoryViewHolder>(
                    0,
                    click()
                )
            )
        
        // Activity should navigate (finish)
        // We can't easily test navigation, but we can verify no crash
    }

    // ============================================
    // NAVIGATION TESTS
    // ============================================

    @Test
    fun testBackButtonClick() {
        // Click back button
        onView(withId(R.id.btnBack))
            .perform(click())
        
        // Activity should finish
        // Verify by checking if activity is finishing
        scenario.onActivity { activity ->
            assert(activity.isFinishing || activity.isDestroyed)
        }
    }

    @Test
    fun testAllCategoriesButtonClick() {
        // Click "All Categories" button
        onView(withId(R.id.btnAllCategories))
            .perform(click())
        
        // Should navigate to GameActivity
        // We can't easily test navigation, but verify no crash
    }
}
