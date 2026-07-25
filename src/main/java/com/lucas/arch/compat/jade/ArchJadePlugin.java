package com.lucas.arch.compat.jade;

import com.lucas.arch.block.AllosaurusEggBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ArchJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AllosaurusEggServerProvider.INSTANCE, AllosaurusEggBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AllosaurusEggClientProvider.INSTANCE, AllosaurusEggBlock.class);
    }
}