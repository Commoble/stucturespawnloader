Structure Spawn Loader allows datapacks and mods to use json files to add mob spawn entries to structures' spawn lists.

Built jars are available on Curseforge:

https://www.curseforge.com/minecraft/mc-mods/structure-spawn-loader

If a structure has at least one mob spawn entry (either defined by vanilla minecraft, by a mod that adds a structure, or via this utility mod or a similar mod that adds mob spawn entries to structures) for a given category of mob (monster, creature, ambient, etc), then mobs spawning for that category within the bounds of that structure will use the structure's spawn list instead of the local biome's spawn list.


### JSON Rules and Format

* Structure spawn entry jsons must be places in `data/namespace/structure_spawn_loader/path.json`. 
* Structure spawn entry jsons should have a `"structure"` and `"spawner"` field as shown in the example json below.
* Structure spawn entry jsons that lack either of these fields will be ignored, allowing a datapack to disable another mod or datapack's spawn entries by overriding them with an empty `{}` json.
* The recommended naming convention for namespaces and paths is for `namespace` to be the id of the mod or datapack that adds the entry json, and for `path` to be `structuremod/structure/entitymod/entity` for entries that add an entry for `entitymod:entity` in `structuremod:structure`.
* Jsons added by multiple mods or datapacks will override each other in datapack order if they share the same namespace and path.

### Example JSON

* `data/examplemod/structure_spawn_loader/minecraft/village/minecraft/evoker.json`

![](https://i.imgur.com/SKDBmEh.png "")

* `"structure"`: The namespaced ID of the structure type to add the spawn entry to

* `"spawner"`: A spawn entry object:

* `"type"`: The namespaced ID of the entity type to spawn in this structure

* `"weight"`: The relative weight of this spawn entry, compared to other spawn entries. Common monsters have a spawn weight of 100.

* `"minCount"` and `"maxCount"` affect the size of how many mobs in a group will be attempted to be spawned at once. 1-1 or 1-4 is typical.

### Mod Support

Mods can use this mod in their development environment using Cursemaven.
https://www.cursemaven.com/

A data provider for spawn entry jsons is provided to assist mods with datagenerating jsons. Refer to the examplemod in the github source repository for an example of this.