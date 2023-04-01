package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    lateinit var binding: ActivityRemindersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.navHostFragment.findNavController().popBackStack()
                return true
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val loginIntent: Intent = Intent(this@RemindersActivity, AuthenticationActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(loginIntent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
