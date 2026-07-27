package com.lucas.arch.compat.jade;

import com.lucas.arch.block.AllosaurusEggBlock;
import com.lucas.arch.block.PachycephalosaurusEggBlock;
import com.lucas.arch.block.SpinosaurusEggBlock;
import com.lucas.arch.entity.AllosaurusEntity;
import com.lucas.arch.entity.PachycephalosaurusEntity;
import com.lucas.arch.entity.SpinosaurusEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ArchJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Registro Server-Side
        registration.registerBlockDataProvider(AllosaurusEggServerProvider.INSTANCE, AllosaurusEggBlock.class);
        registration.registerBlockDataProvider(AllosaurusEggServerProvider.INSTANCE, PachycephalosaurusEggBlock.class);
        registration.registerBlockDataProvider(AllosaurusEggServerProvider.INSTANCE, SpinosaurusEggBlock.class);
        
        registration.registerEntityDataProvider(AllosaurusServerProvider.INSTANCE, AllosaurusEntity.class);
        registration.registerEntityDataProvider(AllosaurusServerProvider.INSTANCE, PachycephalosaurusEntity.class);
        registration.registerEntityDataProvider(AllosaurusServerProvider.INSTANCE, SpinosaurusEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Registro Client-Side 
        registration.registerBlockComponent(AllosaurusEggClientProvider.INSTANCE, AllosaurusEggBlock.class);
        registration.registerBlockComponent(AllosaurusEggClientProvider.INSTANCE, PachycephalosaurusEggBlock.class);
        registration.registerBlockComponent(AllosaurusEggClientProvider.INSTANCE, SpinosaurusEggBlock.class);

        registration.registerEntityComponent(AllosaurusClientProvider.INSTANCE, AllosaurusEntity.class);
        registration.registerEntityComponent(AllosaurusClientProvider.INSTANCE, PachycephalosaurusEntity.class);
        registration.registerEntityComponent(AllosaurusClientProvider.INSTANCE, SpinosaurusEntity.class);
    }
}