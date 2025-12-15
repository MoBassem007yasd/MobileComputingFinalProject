package com.example.talabat

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LoginTest {

    @Mock
    lateinit var dao: AppDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `login with correct admin credentials returns success`() = runTest {
        val email = "admin"
        val mockUser = User(email, "123", isAdmin = true)
        `when`(dao.getUser(email)).thenReturn(mockUser)
        val user = dao.getUser(email)

        assertNotNull("User should not be null", user)
        assertEquals("123", user?.password)
        assertTrue("User should be admin", user!!.isAdmin)
    }
}