package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.watch.ModelSignalType;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import lombok.Getter;
import lombok.Setter;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class ModelSubscription<M extends Model> {
    @Getter
    private final Principal principal;
    @Getter
    @Setter
    private List<String> ids = new ArrayList<>();

    @Setter
    @Getter
    private Class<? extends Model> modelClass;
    @Getter
    @Setter
    private ModelSignalType type;

    private final Flowable<M> publisher;

    private ObservableEmitter<M> emitter;

    public ModelSubscription(Principal principal, ModelSignalType type) {
        this.principal = principal;
        this.type = type;
        Observable<M> eventUpdateObservable = Observable.create(emitter -> this.emitter = emitter);

        ConnectableObservable<M> connectableObservable = eventUpdateObservable.share().publish();
        connectableObservable.connect();

        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }


    public Flowable<M> getPublisher() {
        return publisher;
    }

    public void publish(Model model) {
        emitter.onNext((M) model);
    }
}
