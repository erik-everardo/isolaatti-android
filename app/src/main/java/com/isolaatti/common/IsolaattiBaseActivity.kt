package com.isolaatti.common

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.isolaatti.R
import com.isolaatti.connectivity.NetworkStatus
import com.isolaatti.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
abstract class IsolaattiBaseActivity : AppCompatActivity()  {

    val errorViewModel: ErrorMessageViewModel by viewModels()

    private var snackbarNetworkStatus: Snackbar? = null
    private var snackbarError: Snackbar? = null
    private var connectionHasBeenLost: Boolean = false

    private val errorObserver: Observer<Resource.Error.ErrorType?> = Observer {
        when(it) {
            Resource.Error.ErrorType.AuthError -> {
            // TODO
            }
            Resource.Error.ErrorType.NetworkError -> showNetworkErrorMessage()
            Resource.Error.ErrorType.NotFoundError -> showNotFoundErrorMessage()
            Resource.Error.ErrorType.ServerError -> showServerErrorMessage()
            Resource.Error.ErrorType.OtherError -> showUnknownErrorMessage()
            else -> {}
        }
        errorViewModel.error.postValue(null)
    }

    private val connectivityObserver: Observer<Boolean> = Observer { networkAvailable ->
        val view: View = window.decorView.findViewById(android.R.id.content) ?: return@Observer

        if(!networkAvailable) {
            connectionHasBeenLost = true
            snackbarNetworkStatus = Snackbar.make(view, R.string.network_conn_lost, Snackbar.LENGTH_INDEFINITE)
            snackbarNetworkStatus?.show()
        } else if(connectionHasBeenLost) {
            snackbarNetworkStatus?.dismiss()
            snackbarNetworkStatus = Snackbar.make(view, R.string.network_conn_restored, Snackbar.LENGTH_SHORT)
            snackbarNetworkStatus?.show()
            connectionHasBeenLost = false

            CoroutineScope(Dispatchers.Default).launch {
                errorViewModel.askRetry()
            }

        }
    }

    private val signInActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
            CoroutineScope(Dispatchers.Default).launch {
                errorViewModel.handleRetry()
            }
        }
    }

    private fun showNetworkErrorMessage() {
        val view: View = window.decorView.findViewById(android.R.id.content) ?: return


        snackbarError = Snackbar.make(view, R.string.network_error, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) {
                CoroutineScope(Dispatchers.Default).launch {
                    errorViewModel.askRetry()
                }
                errorViewModel.error.postValue(null)
            }

        snackbarError?.show()
    }

    private fun showServerErrorMessage() {
        val view: View = window.decorView.findViewById(android.R.id.content) ?: return


        snackbarError = Snackbar.make(view, R.string.server_error, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) {
                CoroutineScope(Dispatchers.Default).launch {
                    errorViewModel.askRetry()
                }
                errorViewModel.error.postValue(null)
            }

        snackbarError?.show()
    }

    private fun showNotFoundErrorMessage() {
        val view: View = window.decorView.findViewById(android.R.id.content) ?: return


        snackbarError = Snackbar.make(view, R.string.not_found, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) {
                CoroutineScope(Dispatchers.Default).launch {
                    errorViewModel.askRetry()
                }
                errorViewModel.error.postValue(null)
            }

        snackbarError?.show()
    }

    private fun showUnknownErrorMessage() {
        val view: View = window.decorView.findViewById(android.R.id.content) ?: return


        snackbarError = Snackbar.make(view, R.string.unknown_error, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) {
                CoroutineScope(Dispatchers.Default).launch {
                    errorViewModel.askRetry()
                }
                errorViewModel.error.postValue(null)
            }

        snackbarError?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        errorViewModel.error.observe(this, errorObserver)
        NetworkStatus.networkIsAvailable.observe(this, connectivityObserver)


        Log.d("IsolaattiBaseActivity", errorViewModel.toString())
    }
}