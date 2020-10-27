package com.softwareverde.bch.giftcards.webserver.api.v1.post;

import com.softwareverde.bch.giftcards.database.DatabaseManager;
import com.softwareverde.bch.giftcards.webserver.Environment;
import com.softwareverde.bch.giftcards.webserver.NewAddressCreatedCallback;
import com.softwareverde.bch.giftcards.webserver.api.endpoint.RedeemApi;
import com.softwareverde.bitcoin.address.Address;
import com.softwareverde.bitcoin.address.AddressInflater;
import com.softwareverde.bitcoin.inflater.MasterInflater;
import com.softwareverde.bitcoin.wallet.Wallet;
import com.softwareverde.cryptography.secp256k1.key.PrivateKey;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.JsonResponse;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.http.server.servlet.routed.RequestHandler;
import com.softwareverde.http.server.servlet.routed.json.JsonRequestHandler;
import com.softwareverde.json.Json;
import com.softwareverde.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NewRedemptionAddressHandler implements RequestHandler<Environment> {
    private static final String GIFT_CARD_AMOUNT_FIELD_NAME = "giftCardAmount";

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
            final Json requestData = JsonRequestHandler.getRequestDataAsJson(request);

            final String giftCardAmountString = requestData.getString(GIFT_CARD_AMOUNT_FIELD_NAME);
            if (Util.isBlank(giftCardAmountString)) {
                return _validationError("Missing Field: " + GIFT_CARD_AMOUNT_FIELD_NAME);
            }
            final double giftCardAmount = Util.parseDouble(giftCardAmountString, -1D);
            if (giftCardAmount <= 0) {
                return _validationError("Invalid gift card amount: " + giftCardAmountString);
            }

            formData.put(GIFT_CARD_AMOUNT_FIELD_NAME, giftCardAmount);

            final List<String> requiredFields = _getRequiredFields();
            for (final String fieldName : requiredFields) {
                final String fieldValue = requestData.getString(fieldName);
                if (Util.isBlank(fieldValue)) {
                    return _validationError("Missing Field: " + fieldName);
                }

                formData.put(fieldName, fieldValue);
            }

            final List<String> optionalFields = _getOptionalFields();
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

    private Response _validationError(final String errorMessage) {
        final RedeemApi.NewRedemptionAddressResult newRedemptionAddressResult = new RedeemApi.NewRedemptionAddressResult();
        newRedemptionAddressResult.setWasSuccess(false);
        newRedemptionAddressResult.setErrorMessage(errorMessage);
        return new JsonResponse(Response.Codes.BAD_REQUEST, newRedemptionAddressResult);
    }

    private List<String> _getRequiredFields() {
        return Arrays.asList("emailAddress");
    }

    private List<String> _getOptionalFields() {
        return Arrays.asList();
    }
}
