You're absolutely right! Without visual implementation first, we'd have no way to see if the entity is working correctly. Let me reorder the plan logically:

## Revised Incremental Plan for Rideable Hoverboard Entity

### Phase 1: Basic Entity and Visual Implementation
**Goal**: Create a visible hoverboard entity that spawns and renders correctly

1. **Update HoverboardEntity.java**
   - Make it extend `Entity` with basic required methods
   - Add entity data synchronization for position and rotation
   - Set appropriate collision box size (stone slab dimensions: 0.5f height, 1.0f width)
   - Implement basic hover mechanics to maintain ~0.5 blocks above ground
   - Basic `tick()` method with hover height maintenance

2. **Create HoverboardRenderer.java**
   - Follow pattern from `GravityAnomalyRenderer.java`
   - Use Minecraft's stone slab model/texture
   - Place in `src/main/java/com/hexvane/strangematter/client/`
   - Implement proper positioning and rotation

3. **Register Entity and Renderer**
   - Add `HOVERBOARD_ENTITY` to `ENTITY_TYPES` register in `StrangeMatterMod.java`
   - Register renderer in `StrangeMatterMod.ClientModEvents.onClientSetup()`
   - Update `HoverboardItem.java` to spawn actual `HoverboardEntity` instead of boat

### Phase 2: Riding Mechanics
**Goal**: Make the hoverboard rideable with mount/dismount functionality

4. **Add Riding System**
   - Implement player riding mechanics using `addPassenger()` and `removePassenger()`
   - Add right-click interaction to mount the hoverboard
   - Implement dismount mechanics (sneak key or right-click)
   - Ensure proper player positioning on the hoverboard

### Phase 3: Movement and Controls
**Goal**: Implement forward/backward movement with keyboard controls

5. **Add Movement System**
   - Implement player input handling for W/S keys in `tick()` method
   - Add momentum and friction for realistic movement
   - Add collision detection with blocks and entities
   - Ensure hoverboard maintains proper hover height during movement

6. **Network Synchronization**
   - Add movement data synchronization between client/server
   - Ensure smooth client-side prediction
   - Handle multiplayer riding properly

### Phase 4: Polish and Integration
**Goal**: Finalize and integrate with existing mod systems

7. **Add Model and Texture Resources**
   - Create/reference stone slab model in `assets/strangematter/models/entity/`
   - Use existing Minecraft stone slab texture or create custom variant
   - Add proper resource location references

8. **Sound Integration**
   - Add hoverboard sounds using existing `StrangeMatterSounds` system
   - Implement hover/movement sound effects
   - Follow pattern from other entities in the mod

9. **Final Testing and Refinement**
   - Test all mechanics together
   - Verify visual appearance and hover height
   - Ensure proper collision and physics
   - Test multiplayer synchronization

This way, after Phase 1, you'll be able to see the hoverboard entity in the world, verify it's hovering correctly, and confirm the basic systems are working before adding the more complex riding and movement mechanics.