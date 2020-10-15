package com.softwareverde.bch.giftcards.webserver.api.endpoint;

import com.softwareverde.bch.giftcards.configuration.BitcoinProperties;
import com.softwareverde.bch.giftcards.configuration.ServerProperties;
import com.softwareverde.bitcoin.server.module.node.rpc.NodeJsonRpcConnection;
import com.softwareverde.bitcoin.transaction.Transaction;
import com.softwareverde.bitcoin.transaction.output.TransactionOutput;
import com.softwareverde.concurrent.pool.ThreadPool;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.hash.sha256.Sha256Hash;
import com.softwareverde.http.server.servlet.WebSocketServlet;
import com.softwareverde.http.server.servlet.request.WebSocketRequest;
import com.softwareverde.http.server.servlet.response.WebSocketResponse;
import com.softwareverde.http.websocket.WebSocket;
import com.softwareverde.json.Json;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.RotatingQueue;
import com.softwareverde.util.Util;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AnnouncementsApi implements WebSocketServlet {
    protected static final Object MUTEX = new Object();
    protected static final HashMap<Long, WebSocket> WEB_SOCKETS = new HashMap<Long, WebSocket>();

    protected static final ReentrantReadWriteLock.ReadLock QUEUE_READ_LOCK;
    protected static final ReentrantReadWriteLock.WriteLock QUEUE_WRITE_LOCK;
    static {
        final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        QUEUE_READ_LOCK = readWriteLock.readLock();
        QUEUE_WRITE_LOCK = readWriteLock.writeLock();
    }

    protected static final RotatingQueue<Transaction> TRANSACTIONS = new RotatingQueue<Transaction>(32);

    protected static final AtomicLong _nextSocketId = new AtomicLong(1L);

    protected final ThreadPool _threadPool;
    protected final ServerProperties _serverProperties;
    protected final BitcoinProperties _bitcoinProperties;
    protected final Object _socketConnectionMutex = new Object();

    protected Boolean _shouldBroadcastOldTransactionsOnConnect = true;
    protected Boolean _isShuttingDown = false;
    protected NodeJsonRpcConnection _socketConnection = null;

    protected Json _createTransactionJson(final Transaction transaction) {
        final Sha256Hash transactionHash = transaction.getHash();

        final Json json = new Json(false);
        json.put("hash", transactionHash);

        {
            final Json outputsJson = new Json(true);
            final List<TransactionOutput> transactionOutputs = transaction.getTransactionOutputs();
            for (int outputIndex = 0; outputIndex < transactionOutputs.getCount(); ++outputIndex) {
                final TransactionOutput transactionOutput = transactionOutputs.get(outputIndex);
                final Json transactionOutputJson = transactionOutput.toJson();
                outputsJson.add(transactionOutputJson);
            }
            json.put("outputs", outputsJson);
        }

        return json;
    }

    protected void _broadcastNewTransaction(final Transaction transaction) {
        final Json transactionJson = _createTransactionJson(transaction);
        final String message = transactionJson.toString();

        synchronized (MUTEX) {
            for (final WebSocket webSocket : WEB_SOCKETS.values()) {
                webSocket.sendMessage(message);
            }
        }
    }

    public AnnouncementsApi(final ServerProperties serverProperties, final BitcoinProperties bitcoinProperties, final ThreadPool threadPool) {
        _serverProperties = serverProperties;
        _bitcoinProperties = bitcoinProperties;
        _threadPool = threadPool;
    }

    @Override
    public WebSocketResponse onRequest(final WebSocketRequest webSocketRequest) {
        final WebSocketResponse webSocketResponse = new WebSocketResponse();
        if (! _isShuttingDown) {
            final Long webSocketId = _nextSocketId.getAndIncrement();
            webSocketResponse.setWebSocketId(webSocketId);
            webSocketResponse.upgradeToWebSocket();
        }
        return webSocketResponse;
    }

    @Override
    public void onNewWebSocket(final WebSocket webSocket) {
        if (_isShuttingDown) {
            webSocket.close();
            return;
        }

        final Long webSocketId = webSocket.getId();
        synchronized (MUTEX) {
            Logger.debug("Adding WebSocket: " + webSocketId + " (count=" + (WEB_SOCKETS.size() + 1) + ")");
            WEB_SOCKETS.put(webSocketId, webSocket);
        }

        // webSocket.setMessageReceivedCallback(new WebSocket.MessageReceivedCallback() {
        //     @Override
        //     public void onMessage(final String message) {
        //         // Nothing.
        //     }
        // });

        webSocket.setConnectionClosedCallback(new WebSocket.ConnectionClosedCallback() {
            @Override
            public void onClose(final int code, final String message) {
                synchronized (MUTEX) {
                    Logger.debug("WebSocket Closed: " + webSocketId + " (count=" + (WEB_SOCKETS.size() - 1) + ")");
                    WEB_SOCKETS.remove(webSocketId);
                }
            }
        });

        // webSocket.startListening();

        if (_shouldBroadcastOldTransactionsOnConnect) {
            try {
                QUEUE_READ_LOCK.lock();

                for (final Transaction transaction : TRANSACTIONS) {
                    final Json transactionJson = _createTransactionJson(transaction);
                    final String message = transactionJson.toString();
                    webSocket.sendMessage(message);
                }
            }
            finally {
                QUEUE_READ_LOCK.unlock();
            }
        }
    }

    public void onNewTransaction(final Transaction transaction) {
        try {
            QUEUE_WRITE_LOCK.lock();

            // Check for duplicates...
            for (final Transaction existingTransaction : TRANSACTIONS) {
                if (Util.areEqual(transaction, existingTransaction)) {
                    return;
                }
            }

            TRANSACTIONS.add(transaction);
        }
        finally {
            QUEUE_WRITE_LOCK.unlock();
        }

        _broadcastNewTransaction(transaction);
    }

    public void setShouldBroadcastOldTransactionsOnConnect(final Boolean shouldBroadcastOldTransactionsOnConnect) {
        _shouldBroadcastOldTransactionsOnConnect = shouldBroadcastOldTransactionsOnConnect;
    }

    public Boolean shouldBroadcastOldTransactionsOnConnect() {
        return _shouldBroadcastOldTransactionsOnConnect;
    }

    public void shutdown() {
        _isShuttingDown = true;

        synchronized (_socketConnectionMutex) {
            if (_socketConnection != null) {
                _socketConnection.close();
            }
        }

        synchronized (MUTEX) {
            for (final WebSocket webSocket : WEB_SOCKETS.values()) {
                webSocket.close();
            }
            WEB_SOCKETS.clear();
        }
    }
}
