#pragma once
#include <types.h>
namespace zenith::gpu {
    using PFN_vkGetInstanceProcAddr = void*;
    using LinkerObject = void*;

    class RenderEngine {
    public:
        RenderEngine() = default;
        void operator=(LinkerObject devDriver) {
            this->driver = devDriver;
        }

        LinkerObject driver{};
        PFN_vkGetInstanceProcAddr vulkanInstanceAddr{};

        bool loadVulkanDriver();
    };
}