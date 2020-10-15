package com.softwareverde.bch.giftcards.webserver.api.v1.post;

import com.softwareverde.bch.giftcards.GiftCardService;
import com.softwareverde.bch.giftcards.database.DatabaseManager;
import com.softwareverde.bch.giftcards.nodeapi.NodeConnection;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.NewAddressCreatedCallback;
import com.softwareverde.bch.giftcards.webserver.api.endpoint.RedeemApi;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.address.AddressInflater;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.slp.SlpTokenId;
import com.softwareverde.bitcoin.wallet.Wallet;
import com.softwareverde.constable.list.List;
import com.softwareverde.cryptography.secp256k1.key.PrivateKey;
import com.softwareverde.http.querystring.PostParameters;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.JsonResponse;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.RequestHandler;
import com.softwareverde.json.Json;
import com.softwareverde.util.Util;

import java.util.Map;

public class NewRedemptionAddressHandler implements RequestHandler<Environment> {
    protected final NewAddressCreatedCallback _newAddressCreatedCallback;

    public NewRedemptionAddressHandler(final NewAddressCreatedCallback newAddressCreatedCallback) {
        _newAddressCreatedCallback = newAddressCreatedCallback;
    }

    @Override
    public Response handleRequest(final Request request, final Environment environment, final Map<String, String> parameters) throws Exception {
        final MasterInflater masterInflater = environment.getMasterInflater();
        final AddressInflater addressInflater = masterInflater.getAddressInflater();

        final Json formData = new Json(false);
        {
            final List<String> requiredFields = null; //redemptionItem.getRequiredFields();
            final PostParameters postParameters = request.getPostParameters();
            for (final String fieldName : requiredFields) {
                final String fieldValue = postParameters.get(fieldName);
                if (Util.isBlank(fieldValue)) {
                    final RedeemApi.NewRedemptionAddressResult newRedemptionAddressResult = new RedeemApi.NewRedemptionAddressResult();
                    newRedemptionAddressResult.setWasSuccess(false);
                    newRedemptionAddressResult.setErrorMessage("Missing Field: " + fieldName);
                    return new JsonResponse(Response.Codes.BAD_REQUEST, newRedemptionAddressResult);
                }

                formData.put(fieldName, fieldValue);
            }

            final List<String> optionalFields = null; //redemptionItem.getOptionalFields();
            for (final String fieldName : optionalFields) {
                final String fieldValue = parameters.get(fieldName);
                formData.put(fieldName, fieldValue);
            }
        }

        final Address address;
        try (final DatabaseManager databaseManager = environment.newDatabaseManager()) {
            final PrivateKey privateKey = PrivateKey.createNewKey();
            address = addressInflater.fromPrivateKey(privateKey, true);

            databaseManager.storeRedemptionAddress(privateKey, address, formData);

            final Wallet spvWallet = environment.getSpvWallet();
            spvWallet.addPrivateKey(privateKey);

            if (_newAddressCreatedCallback != null) {
                _newAddressCreatedCallback.newAddressCreated(address);
            }
        }

        final RedeemApi.NewRedemptionAddressResult newRedemptionAddressResult = new RedeemApi.NewRedemptionAddressResult();
        newRedemptionAddressResult.setWasSuccess(true);
        newRedemptionAddressResult.setAddress(address);
        return new JsonResponse(Response.Codes.OK, newRedemptionAddressResult);
    }
}
