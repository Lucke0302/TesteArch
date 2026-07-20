package com.lucas.arch.recipe;

import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Define o resultado possível de processar um fóssil na Cleansing Table.
 *
 * successOutput: item devolvido quando o processo dá certo (pode ser null se ainda não existe).
 * failureOutputs: pool de itens "lixo" que podem sair quando o processo falha, cada um com um peso
 *                  (peso maior = mais chance de sair aquele item específico dentro do grupo de falha).
 */
public record CleansingRecipe(Item input, Item successOutput, List<WeightedItem> failureOutputs) {

    public record WeightedItem(Item item, int weight) {
    }
}