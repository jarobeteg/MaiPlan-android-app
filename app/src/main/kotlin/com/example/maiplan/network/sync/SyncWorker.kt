package com.example.maiplan.network.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.network.TideHttpException
import com.example.maiplan.network.TideProtocolException
import com.example.maiplan.utils.AppVisibilityTracker
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.notifications.ReminderCoordinator
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import java.io.IOException

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)

        if (!sessionManager.hasSession()) {
            sessionManager.clearSession()
            UserSession.clear()
            return Result.failure()
        }

        if (!sessionManager.hasUsableAccessToken() &&
            !AppVisibilityTracker.isAppInForeground) {
            return Result.failure()
        }

        return try {
            val activeUserSyncId = sessionManager.getActiveUserSyncId()
                ?: run {
                    sessionManager.clearSession()
                    UserSession.clear()
                    return Result.failure()
                }

            val activeUser = MaiPlanDatabase.getDatabase(applicationContext)
                .userDAO()
                .getActiveUserBySyncId(activeUserSyncId)

            if (activeUser == null) {
                sessionManager.clearSession()
                UserSession.clear()
                return Result.failure()
            }

            UserSession.setup(activeUser)

            val manager = ServiceLocator.provideSyncManager(applicationContext)
            val hasMoreWork = manager.syncAll(
                userLocalId = activeUser.userLocalId,
                userSyncId = activeUser.syncId
            )

            ReminderCoordinator(applicationContext).recover(activeUser.userLocalId)

            if (hasMoreWork) Result.retry() else Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: TideHttpException) {
            Log.w(TAG, "Sync HTTP failure on attempt ${runAttemptCount + 1}", exception)
            if (exception.statusCode.isRetryableTideStatus()) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (exception: TideProtocolException) {
            Log.e(TAG, "Sync protocol failure", exception)
            Result.failure()
        } catch (exception: TideResponseValidationException) {
            Log.e(TAG, "Sync response validation failure", exception)
            Result.failure()
        } catch (exception: JsonParseException) {
            Log.e(TAG, "Sync JSON response parsing failure", exception)
            Result.failure()
        } catch (exception: IOException) {
            Log.w(TAG, "Sync transport failure on attempt ${runAttemptCount + 1}", exception)
            Result.retry()
        } catch (exception: Exception) {
            Log.e(TAG, "Sync processing failure on attempt ${runAttemptCount + 1}", exception)
            Result.retry()
        }
    }

    private fun Int.isRetryableTideStatus(): Boolean {
        return this == 408 || this == 425 || this == 429 || this in 500..599
    }

    private companion object {
        const val TAG = "TIDESync"
    }
}
