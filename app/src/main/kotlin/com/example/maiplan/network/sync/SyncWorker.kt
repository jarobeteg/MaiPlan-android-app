package com.example.maiplan.network.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.maiplan.database.MaiPlanDatabase
import com.example.maiplan.network.TideHttpException
import com.example.maiplan.network.TideProtocolException
import com.example.maiplan.utils.AppVisibilityTracker
import com.example.maiplan.utils.SessionManager
import com.example.maiplan.utils.common.UserSession
import com.example.maiplan.utils.notifications.EventAlarmCoordinator
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

        // ForegroundTokenAuthenticator can renew an expired access token while the app is open.
        // A background run cannot renew it, so report that run as incomplete.
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

            EventAlarmCoordinator(applicationContext).reconcileAll(activeUser.userLocalId)

            if (hasMoreWork) Result.retry() else Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: TideHttpException) {
            if (exception.statusCode.isRetryableTideStatus()) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (_: TideProtocolException) {
            Result.failure()
        } catch (_: TideResponseValidationException) {
            Result.failure()
        } catch (_: IOException) {
            Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun Int.isRetryableTideStatus(): Boolean {
        return this == 408 || this == 425 || this == 429 || this in 500..599
    }
}
