package org.tbk.bitcoin.example.payreq.invoice;

public enum Status {

    /**
     * Created, but not payed yet. Still changeable.
     */
    CREATED,

    /**
     * {@link Invoice} is ready but not payed yet. Still changeable.
     */
    READY,

    /**
     * The {@link Invoice} is currently processed. No changes allowed to it anymore.
     */
    IN_PROGRESS,

    /**
     * The {@link Invoice} is completed successfully.
     */
    COMPLETED,

	/**
	 * The {@link Invoice} is completed in an error state.
	 */
	FAILED;
}