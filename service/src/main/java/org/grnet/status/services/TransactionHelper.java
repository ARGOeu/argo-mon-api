package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

@ApplicationScoped
public class TransactionHelper {

    @Inject
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public void sendAfterCommit(Runnable action) {

        transactionSynchronizationRegistry.registerInterposedSynchronization(
                new Synchronization() {

                    @Override
                    public void beforeCompletion() {
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == Status.STATUS_COMMITTED) {
                            try {
                                action.run();
                            } catch (Exception e) {
                                Log.error(
                                        "Failed to execute after-commit action",
                                        e
                                );
                            }
                        }
                    }
                }
        );
    }
}
