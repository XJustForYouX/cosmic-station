#include <java/device_res.h>

namespace zenith::java {
    std::weak_ptr<os::OSMachState> JvmManager::getOSState() {
        return m_deviceState;
    }
}