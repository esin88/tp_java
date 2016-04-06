package pinger.requests;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Solovyev on 05/04/16.
 */
public class PingData {
    public static class Request {
        private final long id;

        public Request(long id) {
            this.id = id;
        }

        @NotNull
        public static Builder create() {
            return new Builder();
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        public static class Builder {
            private long _id = -1L;

            @SuppressWarnings({"ParameterHidesMemberVariable", "InstanceMethodNamingConvention"})
            @NotNull
            public Builder id (long id){
                this._id = id;
                return this;
            }
            @NotNull
            public Request build() {
              return new Request(_id);
            }
        }
    }
    public static class Response {
        private long id;
        private long timestamp;

        public long getId() {
            return id;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
