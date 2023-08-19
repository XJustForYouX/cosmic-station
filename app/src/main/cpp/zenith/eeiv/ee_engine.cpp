#include <eeiv/ee_engine.h>
#include <eeiv/cop0.h>

namespace eeiv {
    EmotionMIPS::EmotionMIPS() {
        // Allocating 32 megabytes of RAM to the primary CPU
        // In a simulated hardware environment, we could simply create an array of bytes to serve
        // as our RAM without any issues
        mainRamBlock = new uint8_t[32 * 1024 * 1024];
        gprs = new eeRegister[countOfGPRs];

        resetCore();
    }

    EmotionMIPS::~EmotionMIPS() {
        delete[] mainRamBlock;
        delete[] gprs;
    }

    void EmotionMIPS::resetCore() {
        // The BIOS should be around here somewhere
        regPC = 0xbfc00000;

        // The first register of the EE is register zero, and its value is always zero. Any attempt
        // to write to it is discarded by default
        gprs[0].qw = 0;

        // Signals to the BIOS that the EE is in its reset process, so it will start our registers
        coCPU0.pRid = 0x2e20;
    }
}
