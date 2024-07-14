package com.courtega.advancedhoppers.listener

import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class ExpressionParser {
    private val enchantLevelDelimiter: Char = ':'
    private val orOperator: Char = '|'
    private val andOperator: Char = '&'
    private val notOperator: Char = '!'
    private val containsOperator: Char = '*'
    private val startsOperator: Char = '^'
    private val endsOperator: Char = '$'
    private val tagMarker: Char = '#'
    private val enchantmentMarker: Char = '+'
    val resetCommand: String = ":reset"

    fun isItemProhibited(itemStack: ItemStack, filterPattern: String): Boolean {
        var orSubOperandBooleanResult = false

        nextPatternGroup@ for (orOperand: String in filterPattern.split(orOperator)) {
            for (rawAndOperand: String in orOperand.split(andOperator)) {
                val andOperand = rawAndOperand.lowercase().trim()
                val itemName: String = itemStack.type.key.key

                val filterPatternPrefix: Char = andOperand[0]
                val subOperand: String = andOperand.substring(1)

                val permitted: Boolean = when (filterPatternPrefix) {
                    notOperator -> isItemProhibited(itemStack, subOperand)
                    containsOperator -> itemName.contains(subOperand)
                    startsOperator -> itemName.startsWith(subOperand)
                    endsOperator -> itemName.endsWith(subOperand)
                    tagMarker -> itemStack.containsTag(subOperand)
                    enchantmentMarker -> itemStack.containsEnchantment(subOperand)
                    else -> andOperand == itemName
                }
                if (permitted) {
                    orSubOperandBooleanResult = true
                } else {
                    continue@nextPatternGroup
                }
            }
            if (orSubOperandBooleanResult) return false
        }
        return true
    }

    // region Exposed Methods
    fun getConstants(): Map<String, String> {
        val constants: MutableMap<String, String> = HashMap()
        constants["reset"] = resetCommand
        constants["or"] = orOperator.toString()
        constants["and"] = andOperator.toString()
        constants["not"] = notOperator.toString()
        constants["contains"] = containsOperator.toString()
        constants["starts"] = startsOperator.toString()
        constants["ends"] = endsOperator.toString()
        constants["tag"] = tagMarker.toString()
        constants["enchantment"] = enchantmentMarker.toString()
        return constants
    }

    private fun ItemStack.containsTag(tagName: String): Boolean {
        // The slash character in, for example, mineable/pickaxe, is substituted for underscore
        var tag: Tag<Material>? =
            Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tagName), Material::class.java)
        if (tag == null) {
            tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft(tagName), Material::class.java)
        }
        return (tag != null) && tag.isTagged(this.type)
    }

    private fun ItemStack.containsEnchantment(enchantmentName: String): Boolean {
        val enchantmentMap: Map<Enchantment, Int> = if (this.type == Material.ENCHANTED_BOOK) {
            (this.itemMeta as EnchantmentStorageMeta).storedEnchants
        } else {
            this.enchantments
        }

        val enchantmentNameElements: List<String> = enchantmentName.split(enchantLevelDelimiter)
        if (enchantmentNameElements.size > 2 || enchantmentNameElements.isEmpty()) {
            return false
        }

        val isLevelScrupulous: Boolean = if (enchantmentNameElements.size == 1) {
            false
        } else {
            enchantmentNameElements[1].matches(Regex("-?\\d+"))
        }

        val enchantment: Enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantmentNameElements[0]))
            ?: return false

        val thisEnchantmentLevel: Int = enchantmentMap[enchantment]
            ?: return false

        return if (isLevelScrupulous) {
            thisEnchantmentLevel == enchantmentNameElements[1].toInt()
        } else {
            true
        }

    }
}