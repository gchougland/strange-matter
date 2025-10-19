# Research Points Conditions for Execute Commands

The Strange Matter mod now provides a command that can be used with `/execute if` to check player research points as a condition.

## Quick Start Tutorial

**Step 1: Create a scoreboard objective**
```
/scoreboard objectives add research_check dummy "Research Check"
```

**Step 2: Test the research check command**
```
/researchpoints check @a gravity 100
```
This will tell you how many players have at least 100 gravity research points.

**Step 3: Use it in a command block**
```
/execute store result score @s research_check run researchpoints check @a gravity 100
/execute if score @s research_check matches 1.. run say "Someone has enough gravity research!"
```

**Step 4: Set up command blocks**
- Command Block 1 (Repeating): Store the check result
- Command Block 2 (Chain, Conditional): Execute your command if condition is met

## Basic Usage

The command `/researchpoints check` returns the number of players that meet the research criteria. This return value can be used with `/execute if` for conditional execution.

## Command Syntax

```
/researchpoints check <targets> <researchType> <minPoints> [maxPoints]
```

### Parameters:
- `<targets>`: Target selector (e.g., `@a`, `@p`, `@r`, player name)
- `<researchType>`: One of: `gravity`, `time`, `space`, `energy`, `shadow`, `cognition`
- `<minPoints>`: Minimum research points required
- `<maxPoints>`: (Optional) Maximum research points allowed

## Setup Required

**First, create the scoreboard objectives:**
```
/scoreboard objectives add research_check dummy "Research Check"
/scoreboard objectives add door_check dummy "Door Check"
/scoreboard objectives add range_check dummy "Range Check"
```

## Execute If Integration

### Basic Conditional Execution

**Example 1: Give items to players with enough gravity research**

**Setup Command Block Chain:**
```
Command Block 1 (Repeating, Always Active):
/scoreboard players set @s research_check 0

Command Block 2 (Chain, Always Active):
/execute store result score @s research_check run researchpoints check @a gravity 100

Command Block 3 (Chain, Always Active):
/execute if score @s research_check matches 1.. run give @a minecraft:diamond 1
```

### Advanced Examples

**Example 2: Unlock door for players with specific research**
```
Command Block 1 (Repeating, Always Active):
/scoreboard players set @s door_check 0

Command Block 2 (Chain, Always Active):
/execute store result score @s door_check run researchpoints check @p energy 50

Command Block 3 (Chain, Always Active):
/execute if score @s door_check matches 1.. run setblock ~ ~ ~ minecraft:air

Command Block 4 (Chain, Always Active):
/execute if score @s door_check matches 1.. run playsound minecraft:block.iron_door.open @p
```

**Example 3: Range checking (between 25-75 points)**
```
Command Block 1 (Repeating, Always Active):
/scoreboard players set @s range_check 0

Command Block 2 (Chain, Always Active):
/execute store result score @s range_check run researchpoints check @p time 25 75

Command Block 3 (Chain, Always Active):
/execute if score @s range_check matches 1.. run give @p minecraft:experience_bottle 5
```

**Example 4: Multiple research type checking**
```
# Check if player has both gravity AND energy research
Command Block 1 (Repeating, Always Active):
/scoreboard players set @s gravity_check 0
/scoreboard players set @s energy_check 0

Command Block 2 (Chain, Always Active):
/execute store result score @s gravity_check run researchpoints check @p gravity 100

Command Block 3 (Chain, Always Active):
/execute store result score @s energy_check run researchpoints check @p energy 100

Command Block 4 (Chain, Always Active):
/execute if score @s gravity_check matches 1.. if score @s energy_check matches 1.. run give @p minecraft:nether_star 1
```

## Research Types

Available research types:
- `gravity` - Gravity anomaly research
- `time` - Temporal anomaly research  
- `space` - Spatial anomaly research
- `energy` - Energy anomaly research
- `shadow` - Shadow anomaly research
- `cognition` - Cognitive anomaly research

## Target Selectors

You can use any standard Minecraft target selector:
- `@a` - All players
- `@p` - Nearest player
- `@r` - Random player
- `@s` - Command executor
- `PlayerName` - Specific player by name
- `@a[tag=researcher]` - Players with specific tags
- `@a[team=researchers]` - Players on specific teams

## Scoreboard Integration

The command works perfectly with scoreboards for complex conditional logic:

1. **Store the result** in a scoreboard objective
2. **Use scoreboard conditions** in execute commands
3. **Combine multiple conditions** for complex logic

### Setting up Scoreboard Objectives

```
/scoreboard objectives add research_check dummy "Research Check"
/scoreboard objectives add door_check dummy "Door Check"
/scoreboard objectives add range_check dummy "Range Check"
```

## Permission Requirements

- **Permission Level:** 2 (Operator level)
- **Usage:** Available to server operators and command blocks

## Error Handling

The command provides clear error messages for:
- Invalid research types
- Invalid target selectors
- Permission issues
- Syntax errors

This system provides powerful conditional execution based on player research progress, perfect for creating research-based gameplay mechanics, quests, and progression systems!
