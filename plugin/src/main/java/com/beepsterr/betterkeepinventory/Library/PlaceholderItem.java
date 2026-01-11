package com.beepsterr.betterkeepinventory.Library;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.Depends.PlaceholderAPI;
import com.beepsterr.betterkeepinventory.api.Exceptions.ConditionParseError;
import com.beepsterr.betterkeepinventory.Library.Types.PlaceholderConditionType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlaceholderItem {

    private String placeholder;
    private final PlaceholderConditionType operator;
    private final String value;

    public PlaceholderItem(ConfigurationSection config) throws ConditionParseError {

        if(config.getString("placeholder") == null){
            throw new ConditionParseError(config.getCurrentPath() + ".placeholder", "Placeholder is required");
        }

        if(config.getString("operator") == null){
            throw new ConditionParseError(config.getCurrentPath() + ".operator", "Placeholder operator is required");
        }

        try {

            String value = Objects.requireNonNull(config.getString("operator")).toUpperCase();
            switch(value){
                case "EQUALS":
                case "==":
                case "=":
                    operator = PlaceholderConditionType.EQUALS;
                    break;
                case "!=":
                case "<>":
                case "NOT":
                    operator = PlaceholderConditionType.NOT_EQUALS;
                    break;
                case "INCLUDES":
                    operator = PlaceholderConditionType.CONTAINS;
                    break;
                case ".*":
                case "BEGINS_WITH":
                case "STARTS_WITH":
                    operator = PlaceholderConditionType.STARTS_WITH;
                    break;
                case "*.":
                case "STOPS_WITH":
                case "ENDS_WITH":
                    operator = PlaceholderConditionType.ENDS_WITH;
                    break;
                case ">":
                case "GREATER_THAN":
                    operator = PlaceholderConditionType.GREATER_THAN;
                    break;
                case ">=":
                case "GREATER_THAN_OR_EQUALS":
                    operator = PlaceholderConditionType.GREATER_THAN_OR_EQUALS;
                    break;
                case "<":
                case "LESS_THAN":
                    operator = PlaceholderConditionType.LESS_THAN;
                    break;
                case "<=":
                case "LESS_THAN_OR_EQUALS":
                    operator = PlaceholderConditionType.LESS_THAN_OR_EQUALS;
                    break;
                default:
                    operator = PlaceholderConditionType.valueOf(value);
            }

        } catch (IllegalArgumentException e) {
            throw new ConditionParseError(config.getCurrentPath() + ".operator", "Placeholder operator must be one of " + Objects.toString(PlaceholderConditionType.values()));
        }

        if(config.getString("value") == null){
            throw new ConditionParseError(config.getCurrentPath() + ".value", "Placeholder Value is required");
        }


        this.placeholder = config.getString("placeholder");
        if(!this.placeholder.startsWith("%") && !this.placeholder.endsWith("%")) {
            this.placeholder = "%" + this.placeholder + "%";
        }

        this.value = config.getString("value");

    }

    public boolean test(Player player) {

        BetterKeepInventory.instance.debug(player, "Testing Placeholder Condition: " + this.placeholder + " " + this.operator + " " + this.value);

        // Double check that PlaceholderAPI is still loaded (it could have been unloaded)
        if(!BetterKeepInventory.getInstance().checkDependency("PlaceholderAPI")) return false;

        String resolvedPlaceholder = PlaceholderAPI.setPlaceholders(player, this.placeholder);

        BetterKeepInventory plugin = BetterKeepInventory.getInstance();
        plugin.debug(player, "Going to test placeholder " + resolvedPlaceholder + " against " + this.value + " with operator " + this.operator);

        switch(operator) {
            case EQUALS:
                return resolvedPlaceholder.equals(value);
            case NOT_EQUALS:
                return !resolvedPlaceholder.equals(value);
            case CONTAINS:
                return resolvedPlaceholder.contains(value);
            case STARTS_WITH:
                return resolvedPlaceholder.startsWith(value);
            case ENDS_WITH:
                return resolvedPlaceholder.endsWith(value);
            case GREATER_THAN:
                return Float.parseFloat(resolvedPlaceholder) > Float.parseFloat(value);
            case LESS_THAN:
                return Float.parseFloat(resolvedPlaceholder) < Float.parseFloat(value);
            case GREATER_THAN_OR_EQUALS:
                return Float.parseFloat(resolvedPlaceholder) >= Float.parseFloat(value);
            case LESS_THAN_OR_EQUALS:
                return Float.parseFloat(resolvedPlaceholder) <= Float.parseFloat(value);
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "PlaceholderItem{" +
                "placeholder='" + placeholder + '\'' +
                ", operator=" + operator +
                ", value='" + value + '\'' +
                '}';
    }
}
