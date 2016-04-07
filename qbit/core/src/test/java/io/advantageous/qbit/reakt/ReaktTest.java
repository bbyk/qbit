package io.advantageous.qbit.reakt;

import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.reactive.Reactor;
import io.advantageous.qbit.reactive.ReactorBuilder;
import io.advantageous.reakt.Result;
import io.advantageous.reakt.promise.Promise;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static io.advantageous.qbit.reakt.Reakt.convertQBitCallback;
import static io.advantageous.qbit.reakt.Reakt.convertReaktCallback;
import static io.advantageous.qbit.reakt.Reakt.convertPromise;
import static io.advantageous.reakt.promise.Promise.promise;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class ReaktTest {


    @Test
    public void testConvertPromiseSuccess() throws Exception {

        final AtomicReference<Employee> ref = new AtomicReference<>();
        final Promise<Employee> promise = promise();

        /* Set then callback in promise. */
        promise.then(ref::set);

        /* Convert promise to callback and then call the callback. */
        final io.advantageous.reakt.Callback<Employee> employeeCallback = convertQBitCallback(convertPromise(promise));
        employeeCallback.reply(new Employee("Rick"));

        /* test. */
        assertNotNull(ref.get()); //callback called
        assertTrue(promise.complete()); //done
        assertTrue(promise.success()); //no error
        assertFalse(promise.failure()); // no error
        assertTrue(promise.getRef().isPresent()); //result is present
        assertEquals("Rick", promise.getRef().get().name); //result is Rick
    }

    @Test
    public void testConvertPromiseSuccessUsingReactor() throws Exception {

        final AtomicReference<Employee> ref = new AtomicReference<>();
        final Promise<Employee> promise = promise();

        /* Set then callback in promise. */
        promise.then(ref::set);

        final Reactor reactor = ReactorBuilder.reactorBuilder().build();

        /* Convert promise to callback and then call the callback. */
        final Callback<Employee> employeeCallback = convertPromise(reactor, promise);
        employeeCallback.returnThis(new Employee("Rick"));

        reactor.process();

        /* test. */
        assertNotNull(ref.get()); //callback called
        assertTrue(promise.complete()); //done
        assertTrue(promise.success()); //no error
        assertFalse(promise.failure()); // no error
        assertTrue(promise.getRef().isPresent()); //result is present
        assertEquals("Rick", promise.getRef().get().name); //result is Rick
    }

    @Test
    public void testConvertPromiseFailure() throws Exception {

        final AtomicReference<Employee> ref = new AtomicReference<>();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final Promise<Employee> promise = promise();

        /** Set up success callback and failure callback. */
        promise.then(ref::set)
                .catchError(error::set);

        /** Convert promise to callback and then call the callback with error. */
        final io.advantageous.reakt.Callback<Employee> employeeCallback = convertQBitCallback(convertPromise(promise));

        employeeCallback.fail(new IllegalStateException());

        assertNull(ref.get()); //callback not called
        assertNotNull(error.get()); //error handler called
        assertTrue(promise.complete()); //done
        assertFalse(promise.success()); //error
        assertTrue(promise.failure()); //error

        try {
            assertTrue(promise.getRef().isPresent()); //result access causes exception
            fail();
        } catch (Exception ex) {

        }

        assertNotNull(promise.cause());
    }

    @Test
    public void testConvertCallback() throws Exception {

        final AtomicReference<Result<Employee>> ref = new AtomicReference<>();
        final io.advantageous.reakt.Callback<Employee> callback = ref::set;

        /* Convert promise to callback and then call the callback. */
        final Callback<Employee> employeeCallback = convertReaktCallback(callback);
        employeeCallback.returnThis(new Employee("Rick"));

        final Result<Employee> result = ref.get();

        /* test. */
        assertNotNull(ref.get()); //callback called
        assertTrue(result.complete()); //done
        assertTrue(result.success()); //no error
        assertFalse(result.failure()); // no error
        assertTrue(result.getRef().isPresent()); //result is present
        assertEquals("Rick", result.getRef().get().name); //result is Rick
    }

    @Test
    public void testConvertCallbackUsingReactor() throws Exception {

        final AtomicReference<Result<Employee>> ref = new AtomicReference<>();
        final io.advantageous.reakt.Callback<Employee> callback = ref::set;

        final Reactor reactor = ReactorBuilder.reactorBuilder().build();
        /* Convert promise to callback and then call the callback. */
        final Callback<Employee> employeeCallback = Reakt.convertReaktCallback(reactor, callback);
        employeeCallback.returnThis(new Employee("Rick"));

        reactor.process();

        final Result<Employee> result = ref.get();

        /* test. */
        assertNotNull(ref.get()); //callback called
        assertTrue(result.complete()); //done
        assertTrue(result.success()); //no error
        assertFalse(result.failure()); // no error
        assertTrue(result.getRef().isPresent()); //result is present
        assertEquals("Rick", result.getRef().get().name); //result is Rick
    }

    @Test
    public void testConvertCallbackFailure() throws Exception {


        final AtomicReference<Result<Employee>> ref = new AtomicReference<>();
        final io.advantageous.reakt.Callback<Employee> callback = ref::set;

        /* Convert promise to callback and then call the callback. */
        final Callback<Employee> employeeCallback = convertReaktCallback(callback);
        employeeCallback.returnError("NOT FOUND");

        final Result<Employee> result = ref.get();


        assertNotNull(ref.get()); //callback not called
        assertTrue(result.complete()); //done
        assertFalse(result.success()); //error
        assertTrue(result.failure()); //error

        try {
            assertTrue(result.getRef().isPresent()); //result access causes exception
            fail();
        } catch (Exception ex) {

        }

        assertNotNull(result.cause());

    }

    @Test
    public void testConvertCallbackFailureTimeout() throws Exception {


        final AtomicReference<Result<Employee>> ref = new AtomicReference<>();
        final io.advantageous.reakt.Callback<Employee> callback = ref::set;

        /* Convert promise to callback and then call the callback. */
        final Callback<Employee> employeeCallback = convertReaktCallback(callback);
        employeeCallback.onTimeout();

        final Result<Employee> result = ref.get();


        assertNotNull(ref.get()); //callback not called
        assertTrue(result.complete()); //done
        assertFalse(result.success()); //error
        assertTrue(result.failure()); //error

        try {
            assertTrue(result.getRef().isPresent()); //result access causes exception
            fail();
        } catch (Exception ex) {

        }

        assertNotNull(result.cause());

    }

    static class Employee {
        private final String name;

        Employee(String name) {
            this.name = name;
        }
    }
}