#pragma once

#include <common/types.h>
#include <os/neon_simd.h>
namespace cosmic::gs {
    class GifArk;
}
namespace cosmic::vu {
    struct VuWorkMemory {
        std::array<u8, 1024 * 16> heap;
    };
    union alignas(16) VuRegUnique {
        f32 hd;
        u32 uns;
        std::array<u8, 16> pad;
    };
    union alignas(16) VuReg {
        union {
            float x, y, z, w;
            f32 floats[4];
        };
        u32 uns[4];
        i32 sig[4];
        os::vec128 faster{};
    };
    union alignas(2) VuIntReg {
        VuIntReg() {}
        VuIntReg(i32 halfInt) :
            sig(static_cast<i16>(halfInt)) {}
        i16 sig;
        u16 uns;
    };
    struct VuStatus {
        bool isVuExecuting;
        bool isStartedDivEvent;
    };

    class VuIntPipeline {
    public:
        VuIntPipeline();

        struct PipeEntry {
            u8 affectedIr;
            VuIntReg originalValue;
            bool rw;

            void clearEntry() {
                affectedIr = 0;
                originalValue = {};
                rw = false;
            }
        };
        std::array<PipeEntry, 5> pipeline;
        u8 pipeCurrent;

        void pushInt(u8 ir, VuIntReg old, bool rw);
        void update();
        void flush();
    };

    class VectorUnit {
    public:
        VectorUnit();
        VectorUnit(VectorUnit&) = delete;

        void resetVU();
        void softwareReset();

        void pulse(u32 cycles);
        alignas(512) VuReg VuGPRs[32];
        alignas(32) VuIntReg intsRegs[16];

        void establishVif(u16* conTops, raw_reference<gs::GifArk> gif) {
            vifTops[0] = &conTops[0];
            vifTops[1] = &conTops[1];
            vu1Gif = gif;
        }
        VuRegUnique spI, spQ, spR, spP;
        void ctc(u32 index, u32 value);
        u32 cfc(u32 index);

        VuStatus status;
        VuIntPipeline intPipeline;
        void writeInt(u8 ir, u8 fir);
    private:
        void updateMacPipeline();
        u16 vuf;

        bool isVuBusy, isOnBranch{false};
            // Each pipeline is specialized in a certain domain
        u64 pipeStates[2];

        std::array<u8, 4> clipFlags;
        u8 cfIndex;
        std::array<u16, 4> macFlags;
        u16 nextFlagsPipe;
        u8 mfIndex;

        u32 vuPc{};
        struct {
            i64 count;
            bool isDirty;
        } clock;
        u16* vifTops[2];
        std::optional<raw_reference<gs::GifArk>> vu1Gif;

        VuWorkMemory dataSpace, instSpace;
    };
}
