package io.advantageous.qbit.example.mda;

import io.advantageous.qbit.reactive.Callback;

import java.util.List;

public interface InternalService {

    void getCallStack(Callback<List<String>> listCallback);
}
