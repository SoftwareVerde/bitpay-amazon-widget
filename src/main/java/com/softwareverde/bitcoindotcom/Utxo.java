package com.softwareverde.bitcoindotcom;

import com.softwareverde.cryptography.hash.sha256.Sha256Hash;

public class Utxo {
    private Sha256Hash _transactionHash;
    private Integer _outputIndex;
    private Double _amount;
    private Long _satoshis;
    private Long _blockHeight;
    private Long _confirmations;

    public Sha256Hash getTransactionHash() {
        return _transactionHash;
    }

    public void setTransactionHash(final Sha256Hash transactionHash) {
        this._transactionHash = transactionHash;
    }

    public Integer getOutputIndex() {
        return _outputIndex;
    }

    public void setOutputIndex(final Integer outputIndex) {
        this._outputIndex = outputIndex;
    }

    public Double getAmount() {
        return _amount;
    }

    public void setAmount(final Double amount) {
        this._amount = amount;
    }

    public Long getSatoshis() {
        return _satoshis;
    }

    public void setSatoshis(final Long satoshis) {
        this._satoshis = satoshis;
    }

    public Long getBlockHeight() {
        return _blockHeight;
    }

    public void setBlockHeight(final Long blockHeight) {
        this._blockHeight = blockHeight;
    }

    public Long getConfirmations() {
        return _confirmations;
    }

    public void setConfirmations(final Long confirmations) {
        this._confirmations = confirmations;
    }
}
