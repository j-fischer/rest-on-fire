package org.restonfire;

import com.google.gson.Gson;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Param;
import com.ning.http.client.Response;
import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.restonfire.exceptions.FirebaseInvalidStateException;
import org.restonfire.exceptions.FirebaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfischer on 2016-05-28.
 */
class FirebaseRestQueryImpl implements FirebaseRestQuery {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseRestQueryImpl.class);

  private static final String ORDER_BY = "orderBy";

  private final Gson gson;
  private final String referenceUrl;
  private final AsyncHttpClient.BoundRequestBuilder queryRequest;

  private final HashMap<String, String> queryParams = new HashMap<>();

  FirebaseRestQueryImpl(
    Gson gson,
    AsyncHttpClient.BoundRequestBuilder requestBuilder,
    String referenceUrl
    ) {
    this.gson = gson;
    this.queryRequest = requestBuilder;
    this.referenceUrl = referenceUrl;
  }

  @Override
  public FirebaseRestQuery orderByValue() {
    if (queryParams.containsKey(ORDER_BY)) {
      throw new FirebaseInvalidStateException(FirebaseRuntimeException.ErrorCode.QueryParamAlreadySet, ORDER_BY + " parameter has already been set");
    }

    queryParams.put(ORDER_BY, "\"$value\"");

    return this;
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> run(final Class<T> clazz) {
    LOG.debug("Running query({}) invoked for reference {}. Filters: {}", clazz, referenceUrl, gson.toJson(queryParams));

    final List<Param> params = new ArrayList<>(queryParams.size());

    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      params.add(new Param(entry.getKey(), entry.getValue()));
    }

    queryRequest.addQueryParams(params);

    final Deferred<T, FirebaseRuntimeException, Void> deferred = new DeferredObject<>();

    queryRequest.execute(new AsyncCompletionHandler<Void>() {

      @Override
      public Void onCompleted(Response response) throws Exception {
        try {
          LOG.debug("Request for getValue({}) completed", clazz);
          final T result = handleResponse(response, clazz);
          deferred.resolve(result);
        } catch (FirebaseRuntimeException ex) {
          deferred.reject(ex);
        }
        return null;
      }
    });

    return deferred.promise();
  }

  private <T> T handleResponse(Response response, Class<T> clazz) {
    return RestUtil.handleResponse(gson, referenceUrl, response, clazz);
  }
}
