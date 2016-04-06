package pinger.requests;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Solovyev on 06/04/16.
 */
public class GetPing {
    public static class Request {

    }
    public static class Response {
        private final long ping;
        private final long clientTimeShift;

        public Response(long ping, long clientTimeShift) {
            this.ping = ping;
            this.clientTimeShift = clientTimeShift;
        }

        @NotNull
        public static Builder create() {
            return new Builder();
        }
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public static class Builder {
            private long _ping = -1L;
            private long _timeShift = -1L;

            @SuppressWarnings("ParameterHidesMemberVariable")
            @NotNull
            public Builder ping(long ping){
                this._ping = ping;
                return this;
            }

            @SuppressWarnings("ParameterHidesMemberVariable")
            @NotNull
            public Builder clientTimeShift(long _timeShift) {
                this._timeShift = _timeShift;
                return this;
            }

            public Response build() {
                return new Response(_ping, _timeShift);
            }
        }
    }


}
