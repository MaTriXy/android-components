/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.service.glean.scheduler

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import mozilla.components.service.glean.Glean

/**
 * This class is the worker class used by [WorkManager] to handle uploading the ping to the server.
 * @suppress This is internal only, don't show it in the docs.
 */
class PingUploadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        internal const val PING_WORKER_TAG = "mozac_service_glean_ping_upload_worker"

        /**
         * Build the constraints around which the worker can be run, such as whether network
         * connectivity is required.
         *
         * @return [Constraints] object containing the required work constraints
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun buildConstraints(): Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /**
         * Build the [OneTimeWorkRequest] for enqueueing in the [WorkManager].  This also adds a tag
         * by which enqueued requests can be identified.
         *
         * @return [OneTimeWorkRequest] representing the task for the [WorkManager] to enqueue and run
         */
        internal fun buildWorkRequest(): OneTimeWorkRequest = OneTimeWorkRequestBuilder<PingUploadWorker>()
            .addTag(PING_WORKER_TAG)
            .setConstraints(buildConstraints())
            .build()

        /**
         * Function to aid in properly enqueuing the worker in [WorkManager]
         */
        internal fun enqueueWorker() {
            WorkManager.getInstance().enqueueUniqueWork(
                PING_WORKER_TAG,
                ExistingWorkPolicy.KEEP,
                buildWorkRequest())
        }

        /**
         * Function to perform the actual ping upload task.  This is created here in the
         * companion object in order to facilitate testing.
         *
         * @return true if process was successful
         */
        private fun uploadPings(): Boolean {
            val httpPingUploader = Glean.httpClient
            return Glean.pingStorageEngine.process(httpPingUploader::doUpload)
        }

        /**
         * Function to cancel any pending ping upload workers
         */
        internal fun cancel() {
            WorkManager.getInstance().cancelUniqueWork(PING_WORKER_TAG)
        }
    }

    /**
     * This method is called on a background thread - you are required to **synchronously** do your
     * work and return the [androidx.work.ListenableWorker.Result] from this method.  Once you
     * return from this method, the Worker is considered to have finished what its doing and will be
     * destroyed.
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [androidx.work.ListenableWorker.Result].  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [androidx.work.ListenableWorker.Result] of the computation
     */
    override fun doWork(): Result {
        return when {
            !Glean.getUploadEnabled() -> Result.failure()
            !uploadPings() -> Result.retry()
            else -> Result.success()
        }
    }
}
