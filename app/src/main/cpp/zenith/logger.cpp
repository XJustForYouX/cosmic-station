#include <perfetto.h>
#include <logger.h>

PERFETTO_DEFINE_CATEGORIES(
    perfetto::Category("launch")
        .SetDescription("Metrics obtained through the launch of the application"));

// It will expand into a structure containing all possible events,
// which need to be static in the binary sections
PERFETTO_TRACK_EVENT_STATIC_STORAGE();

namespace zenith {
    void verifyRtCheck(bool condition, std::function<void()> func) {
        [[unlikely]] if (condition)
            func();
    }

    GlobalLogger::GlobalLogger() {
        perfetto::TracingInitArgs args;

        // Write all records to the `traced` daemon, to improve speed and accuracy during tracing
        args.backends |= perfetto::kSystemBackend;
        perfetto::Tracing::Initialize(args);
        perfetto::TrackEvent::Register();
    }
}
