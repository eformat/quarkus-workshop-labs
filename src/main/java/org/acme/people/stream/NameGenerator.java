package org.acme.people.stream;

import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import org.acme.people.utils.CuteNameGenerator;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.reactivex.Flowable;

@ApplicationScoped
public class NameGenerator {

    @Outgoing("generated-name")
    public Flowable<String> generate() {
        return Flowable.interval(5, TimeUnit.SECONDS).map(tick -> CuteNameGenerator.generate());
    }

}
