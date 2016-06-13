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
  private static final String START_AT = "startAt";
  private static final String END_AT = "endAt";
  private static final String EQUAL_TO = "equalTo";
  private static final String LIMIT_FIRST = "limitToFirst";
  private static final String LIMIT_LAST = "limitToLast";

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
  public FirebaseRestQuery startAt(Object val) {
    return setParameter(START_AT, val);
  }

  @Override
  public FirebaseRestQuery endAt(Object val) {
    return setParameter(END_AT, val);
  }

  @Override
  public FirebaseRestQuery equalTo(Object val) {
    return setParameter(EQUAL_TO, val);
  }

  @Override
  public FirebaseRestQuery limitToFirst(int number) {
    return setParameter(LIMIT_FIRST, number);
  }

  @Override
  public FirebaseRestQuery limitToLast(int number) {
    return setParameter(LIMIT_LAST, number);
  }

  @Override
  public FirebaseRestQuery orderByKey() {
    return setParameter(ORDER_BY, "$key");
  }

  @Override
  public FirebaseRestQuery orderByChild(String name) {
    return setParameter(ORDER_BY, name);
  }

  @Override
  public FirebaseRestQuery orderByPriority() {
    return setParameter(ORDER_BY, "$priority");
  }

  @Override
  public FirebaseRestQuery orderByValue() {
    return setParameter(ORDER_BY, "$value");
  }

  @Override
  public void clear() {
    queryParams.clear();
  }

  @Override
  public <T> Promise<T, FirebaseRuntimeException, Void> run(final Class<T> clazz) {
    LOG.debug("Running query({}) invoked for reference {}. Filters: {}", clazz, referenceUrl, gson.toJson(queryParams));

    final List<Param> params = new ArrayList<>(queryParams.size());

    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      params.add(new Param(entry.getKey(), entry.getValue()));
    }

    if (params.size() > 0)
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

  private FirebaseRestQuery setParameter(String key, Object value) {
    if (queryParams.containsKey(key)) {
      throw new FirebaseInvalidStateException(FirebaseRuntimeException.ErrorCode.QueryParamAlreadySet, key + " parameter has already been set");
    }

    queryParams.put(key, gson.toJson(value));

    return this;
  }

  private <T> T handleResponse(Response response, Class<T> clazz) {
    return RestUtil.handleResponse(gson, referenceUrl, response, clazz);
  }
}
