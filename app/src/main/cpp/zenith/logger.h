#pragma once

#include <android/log.h>
#include <vector>

#include <types.h>

namespace zenith {
    enum LoggerLevel {
        Info = ANDROID_LOG_INFO,
        Debug = ANDROID_LOG_DEBUG,
        Verbose = ANDROID_LOG_VERBOSE,
        Error = ANDROID_LOG_ERROR,
    };

    class GlobalLogger {
    public:
        GlobalLogger();
    private:
        ZenFile logFile{};
        // Don't allow these specific levels to be threaded or printed to the user
        std::vector<LoggerLevel> refuseLevels{};
    };
}
