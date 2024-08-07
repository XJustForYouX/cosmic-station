#include <iop/iop_cop.h>
#include <bitset>

namespace cosmic::iop {
    void IopCop::rfe() {
        status.kuc = status.kup;
        status.kup = status.kuo;

        status.iec = status.iep;
        status.iep = status.ieo;
    }
    static const u8 mask{0xff};
    u32 IopCop::mfc(u8 copId) const {
        u32 mcVar{};
        switch (copId) {
        case 12: {
            std::bitset<8 * 4> bin{};
            bin[0] = status.iec;
            bin[1] = status.kuc;
            bin[2] = status.iep;
            bin[3] = status.kup;
            bin[4] = status.ieo;
            bin[5] = status.kuo;
            bin[0x10] = status.isC;
            bin[0x16] = status.bev;

            // bev: needs to be set externally, as its value is not within the range of 8 bytes
            mcVar |= static_cast<u32>(status.imm << 8);
            mcVar |= bin.to_ulong();
        }
            break;
        case 13:
            mcVar |= static_cast<u32>(cause.code << 2);
            mcVar |= static_cast<u32>(cause.intPending << 8);
            mcVar |= static_cast<u32>(cause.bd << 31);
            break;
        case 14:
            mcVar = ePc; break;
        case 15:
            mcVar = c0id; break;
        default:
            mcVar = {};
        }
        return mcVar;
    }
    void IopCop::mtc(u8 copId, u32 regVal) {
        std::bitset<8 * 8> leaf{status.to64()};
        if (copId != 12) {
            throw AppErr("Unknown register with index {} being used", copId);
        }
        leaf[0] = regVal & 1;
        leaf[1] = regVal & (1 << 1);
        leaf[2] = regVal & (1 << 2);
        leaf[3] = regVal & (1 << 3);
        leaf[4] = regVal & (1 << 4);
        leaf[5] = regVal & (1 << 5);
        leaf[6] = (regVal >> 8) & mask;
        leaf[7] = regVal & (1 << 16);
        status.bev = regVal & (1 << 22);

        status.st64(leaf.to_ulong());
    }

    void IopCop::resetIoCop() {
        status = {};
        cause = {};

        status.bev = true;
        cause.intPending = 0;
        ePc = 0;
        c0id = 0x58;
    }
}
