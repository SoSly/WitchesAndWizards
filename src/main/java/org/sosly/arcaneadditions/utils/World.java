/*
 *   Arcane Additions Copyright (c)  2022, Kevin Kragenbrink <kevin@writh.net>
 *           This program comes with ABSOLUTELY NO WARRANTY; for details see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *           This is free software, and you are welcome to redistribute it under certain
 *           conditions; detailed at https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.sosly.arcaneadditions.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.sosly.arcaneadditions.ArcaneAdditions;

import javax.annotation.Nullable;

public class World {
    @Nullable
    public static Entity getLevelEntity(Entity entity) {
        Level level;
        if ((level = ArcaneAdditions.instance.proxy.getClientWorld()) != null) {
            return level.getEntity(entity.getId());
        }
        return null;
    }
}
