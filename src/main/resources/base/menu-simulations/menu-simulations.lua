local menu_simulations = {}

menu_simulations.forest_fire =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-forest-fire.zip",
  length = 60 * 20,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
    local dx = 0
    local dy = 0
    if game.tick % 3000 < 1000 then
      dx = 0.01
    elseif game.tick % 3000 < 2000 then
      dx = -0.01
    end
    if (game.tick + 1500) % 3000 < 1000 then
      dy = 0.01
    elseif (game.tick + 1500) % 3000 < 2000 then
      dy = -0.01
    end
    game.camera_position = {game.camera_position.x + dx*0, game.camera_position.y + dy*0}
  ]]
}

menu_simulations.solar_power_construction =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-solar-power-construction.zip",
  length = 60 * 7,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0

    local blueprint_string_1 = '0eNqd2dtq4zAUBdB/0bNbLOvuXyllcDKmGBw7+FIagv99cmFoodqWznlLQr2KhfaOdXIVh35tz1M3LKK+iu44DrOo365i7j6Gpr9/tlzOrahFt7QnUYihOd3fzWPfTC/nZmh7sRWiG/62X6KW23sh2mHplq59Mo83lz/Dejq00+0PokAhzuN8u2Yc7v/v5rw49WoKcbm9kqF8NdtW/KKqXKpMUiqTsi5J6VxKJymTS8kkZTMp45OUy6R0+gZ9LpW+wZBJqfQNyjLXMmkrd7+rKm3lbvgqpC1Fz6F3wNL0IELL0JMILUuPIrQcPYvQ8vQwQivQ04isqqTHEVqSnkdoVfQ8QkvR8wgtTc8jtAwjjxpYlpFHZH3v++Z4XE9r3yzjFNv3/r8Ud3yuY/edkOvoXUeVjDyDNVKSkWdkVYw8I0sx8owsnbnuKuyvu8l13L5jcx2z7zhGH6A18ow+QFZg9IEET6Qlow+QJcl9oKLOj32+HualeVy7VwdxRpHrIO5oRh2gJTKMOkCWZdQBshyjDpDlyXUQX/eQtw2+2yDKmJLcBnGH8bSOlsgwntahpcipk1FHk7+F444hxy7uWPI2ijuO/K0Sdzx5H8UdRns7D07ujPaGlqSfrqBV0U9X0FL0+oWWptcvtAy9fqFl6fULLUc/XUHL009X0Ar0/kSWK+n9CS3G1NEZYDHGjtBizB2hxRg8QosxeYQWY/QILcbsEVqM4SO0GNNHZHnG9BFajOcZaDGeZ57We/H86aD+8UtDIT7baX5cVXmpXaictM4FF7btH3fkxKI='
    local blueprint_string = '0eNqd191qhDAQBeB3mWt3MTHmx1cppbjbUAIaRWOpLL57XUvpQjOrkzsj5hPmHAi5waWZbD84H6C6gbt2foTq5Qaj+/B1c38X5t5CBS7YFjLwdXtfjV1TD6e+9raBJQPn3+0XVGx5zcD64IKzP8y2mN/81F7ssH4QBTLou3Hd0/n7/1bnJIpzmcG8PjHDzuWyZP8ofpTKd6niIFWoXUocpcQuVR6l2C4lD1Jc71KKnqDWcUrTE8QoQ08Qo1hOjxC1GD1D1OL0EFGrSEixRCyRECNm/XW+vl6ndmrq0A2xeelfSUYdedSRzx111BHPHZ3QA2xGJqEHiMVz8rxF1Hno+XQZQ73tfTbuOMPJ4447KfXmyIhS6o1Z9HrzqEOvd9yh1zvupNQbm1FKvRGryOk9UAaxGL0HqMXpxxVqFfTjCrUEPUfUKuk5opZMyFEilkrIEbN0Qo6YZRJyRCyRJ+SIWSwhx81arwLbnaF6uGJk8GmHcdvFNRPKcMWkUkaZZfkGTxv4MA=='
    local inventory = game.create_inventory(1)
    local stack = inventory[1]
    stack.import_stack(blueprint_string)
    local function build_blueprint(position)
      stack.build_blueprint{ surface = 'nauvis', position = position, force = 'player', force_build = true }
    end

    local tiktok =
    {
      [0.5 * 60] = {-36, -184},
      [1 * 60] = {-67, -184},
      [4 * 60] = {-36, -184-18},
      [math.floor(4.1 * 60)] = {-36+18, -184-18},
      [4.2 * 60] = {-36+18, -184},
      [4.3 * 60] = {-36+18, -184+18},
      [4.4 * 60] = {-36, -184+18},
      [4.5 * 60] = {-67, -184+18},
      [4.6 * 60] = {-67-18, -184+18},
      [4.7 * 60] = {-67-18, -184},
      [4.8 * 60] = {-67-18, -184-18},
      [4.9 * 60] = {-67, -184-18},
    }

    local start_tick = game.tick
    script.on_event(defines.events.on_tick, function()
      local tick_from_start = game.tick - start_tick
      local position = tiktok[tick_from_start]
      if position then build_blueprint(position) end
    end)

  ]],
  update =
  [[
  ]]
}
menu_simulations.lab =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-lab.zip",
  length = 60 * 10,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0.5
  ]],
  update =
  [[
  ]]
}
menu_simulations.burner_city =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-burner-city.zip",
  length = 60 * 10,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0.5
  ]],
  update =
  [[
  ]]
}
menu_simulations.mining_defense =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-mining-defense.zip",
  length = 60 * 15,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
    game.forces.enemy.evolution_factor = 0.11
  ]],
  update =
  [[
  ]]
}

menu_simulations.biter_base_steamrolled =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-biter-base.zip",
  length = 60 * 10,
  init =
  [[
    local surface = game.surfaces.nauvis
    local logo = surface.find_entities_filtered{name = "factorio-logo-11tiles"}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    center = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    surface.daytime = 0
    game.forces.enemy.evolution_factor = 0.3
    surface.peaceful_mode = true

    local count = 120
    script.on_nth_tick(2,
      function()
        count = count - 2
        if count <= 0 then
          for i=0,20 do
            local y = game.camera_position.y - 20 + i * 2
            local x = game.camera_position.x - 40
            local character = surface.create_entity{ name = "character", position = {x, y}, force = "player" }
            character.color = {1, 0, 0, 0.5}
            local tank = surface.create_entity{ name = "tank", position = {x, y}, force = "player" }
            tank.orientation = 0.25
            tank.insert{name = "rocket-fuel", count = 3}
            tank.speed = 0.5
            tank.set_driver(character)
            character.riding_state = { acceleration = defines.riding.acceleration.accelerating, direction = defines.riding.direction.straight }
          end
          script.on_nth_tick(2, nil)
        end
      end)

    local wube_logo_position = {-499.5, 43.5}

    script.on_nth_tick(1,
    function()
      if surface.count_entities_filtered{position = wube_logo_position, radius = 3, name = "tank"} > 0 then
        local tiles = {}
        for x=-3,3 do
          for y=-3,3 do
            table.insert(tiles, {name = "dirt-1", position = {wube_logo_position[1] + x, wube_logo_position[2] + y}})
          end
        end
        surface.set_tiles(tiles)
      end
    end)
  ]]
}

menu_simulations.biter_base_spidertron =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-biter-base.zip",
  length = 60 * 12,
  init =
  [[
    local surface = game.surfaces.nauvis
    local logo = surface.find_entities_filtered{name = "factorio-logo-11tiles"}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    center = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    surface.daytime = 0
    game.forces.enemy.evolution_factor = 1
    surface.peaceful_mode = true

    spider = surface.create_entity{name = "spidertron", position = {logo.position.x - 30, logo.position.y + 60}, force = "player"}
    spider.force.research_all_technologies()
    local grid = spider.grid
    grid.put{name = "fusion-reactor-equipment"}
    grid.put{name = "personal-laser-defense-equipment"}
    grid.put{name = "personal-laser-defense-equipment"}
    grid.put{name = "personal-laser-defense-equipment"}

    spider.insert({name = "rocket", count = 800})

    points =
    {
      {-16, -8},
      {0, -12},
      {16, -8},
      {16, 0},
      {60, 60},
    }

    local bonk = function()
      local k, position = next(points)
      if not k then return end
      points[k] = nil
      local x = position[1] + center[1]
      local y = position[2] + center[2]
      spider.autopilot_destination = {x, y}
    end

    bonk()

    script.on_event(defines.events.on_spider_command_completed, function(event)
      bonk()
    end)

  ]]
}

menu_simulations.biter_base_artillery =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-biter-base.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    logo.destructible = false
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    center = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
    game.forces.enemy.evolution_factor = 0.7
    local bases = {}
    local get = function()
      bases = game.surfaces[1].find_entities_filtered{force = "enemy", position = center, radius = 32}
      for k, v in pairs (bases) do
        local i = math.random(#bases)
        bases[k], bases[i] = bases[i], bases[k]
      end
    end

    local badonk = function(position, fluff)
      local x = position.x + ((math.random() - 0.5) * fluff * 2)
      local y = position.y + ((math.random() - 0.5) * fluff * 2)
      return {x, y}
    end
    get()
    local donk = function()
      local k, base = next(bases)
      if not k then
        if not badink then
          get()
          badink = true
        end
        return
      end
      bases[k] = nil
      if not base.valid then return end
      game.surfaces[1].create_entity{name = "artillery-projectile", position = {center[1]-80, center[2]-80}, force = "player", target = badonk(base.position, base.get_radius()), speed = 1}
    end

    script.on_nth_tick(17, donk)
    script.on_nth_tick(23, donk)
    script.on_nth_tick(29, donk)

    script.on_event(defines.events.on_entity_died, function()
      if not badoob then
        for k, v in pairs (game.surfaces[1].find_enemy_units(center, 32, "player")) do
          if not (v.command and v.command.type == defines.command.go_to_location) then
            v.set_command{type = defines.command.go_to_location, destination  = {center[1] + 80, center[2] + 20}}
          end
        end
      end
    end)

  ]]
}

menu_simulations.biter_base_player_attack =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-biter-base.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    logo.destructible = false
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    center = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.forces.enemy.evolution_factor = 0.5
    game.surfaces[1].peaceful_mode = true
    game.forces.player.research_all_technologies()
    game.surfaces.nauvis.daytime = 0

    local character = game.surfaces[1].create_entity{name = "character", position = {center[1] - 40, center[2] - 20}, force = "player"}
    character.insert{name = "heavy-armor"}
    character.insert{name = "submachine-gun"}
    character.insert{name = "piercing-rounds-magazine", count = 50}
    character.insert{name = "grenade", count = 50}

    points =
    {
      {-20, -8},
      {-20, 8},
      {-40, 20},
      {0, -12},
      {16, -8},
      {16, 0},
      {8, 8},
      {60, 8},
    }

    local distance = function(p_1, p_2)
      local dx = (p_1[1] or p_1.x) - (p_2[1] or p_2.x)
      local dy = (p_1[2] or p_1.y) - (p_2[2] or p_2.y)
      return ((dx * dx) + (dy * dy)) ^ 0.5
    end

    local direction = function(p_1, p_2)

      local d_x = (p_2[1] or p_2.x) - (p_1[1] or p_1.x)
      local d_y = (p_2[2] or p_2.y) - (p_1[2] or p_1.y)
      local angle = math.atan2(d_y, d_x)

      local orientation =  (angle / (2 * math.pi)) - 0.25
      if orientation < 0 then orientation = orientation + 1 end

      local direction = math.floor((orientation * 8) + 0.5)
      if direction == 8 then direction = 0 end
      return direction
    end

    local get_shoot_target = function(entity)
      local enemies = entity.surface.find_entities_filtered{force = "enemy", type = {"unit-spawner", "turret", "unit"}, position = entity.position, radius = 15}
      local closest = entity.surface.get_closest(entity.position, enemies)
      return closest
    end

    local badonk = function(position, fluff)
      local x = position.x + ((math.random() - 0.5) * fluff * 2)
      local y = position.y + ((math.random() - 0.5) * fluff * 2)
      return {x, y}
    end

    script.on_event(defines.events.on_tick, function()
      if not character.valid then return end
      local k, destination = next(points)
      if not k then return end
      local target = {center[1] + destination[1], center[2] + destination[2]}
      if distance(character.position, target) < 1 then
        points[k] = nil
        return
      end

      if game.tick % 17 == 0 then
        local walking_direction = direction(target, character.position)
        character.walking_state = {walking = true, direction = walking_direction}
      end

      if not (shoot_target and shoot_target.valid) or game.tick % 123 == 0 then
        shoot_target = get_shoot_target(character)
      end

      if shoot_target then
        character.shooting_state = {state = defines.shooting.shooting_enemies, position = shoot_target.position}
        if game.tick % 31 == 0 then
          character.surface.create_entity{name = "grenade", position = character.position, speed = 0.3, target = badonk(shoot_target.position, 2), force = "player"}
        end
      else
        character.shooting_state = {state = defines.shooting.not_shooting}
      end

    end)

  ]]
}

menu_simulations.biter_base_laser_defense =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-biter-base.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    logo.destructible = false
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    center = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.forces.enemy.evolution_factor = 0.7
    game.surfaces[1].peaceful_mode = true
    game.forces.player.research_all_technologies()
    game.surfaces.nauvis.daytime = 0

    local character = game.surfaces[1].create_entity{name = "character", position = {center[1] - 40, center[2] + 20}, force = "player"}
    character.insert{name = "power-armor-mk2"}
    local grid = character.get_inventory(defines.inventory.character_armor)[1].grid
    grid.put{name = "exoskeleton-equipment"}
    grid.put{name = "exoskeleton-equipment"}
    for k = 1, 10 do
      grid.put{name = "personal-laser-defense-equipment"}
      grid.put{name = "energy-shield-mk2-equipment"}
      grid.put{name = "battery-mk2-equipment"}
      grid.put{name = "battery-mk2-equipment"}
    end

    for k, equipment in pairs(grid.equipment) do
      if equipment.max_shield > 0 then equipment.shield = equipment.max_shield end
      equipment.energy = equipment.max_energy
    end

    character.insert{name = "submachine-gun"}
    character.insert{name = "uranium-rounds-magazine", count = 50}

    points =
    {
      {-16, -8},
      {0, -12},
      {16, -8},
      {16, 0},
      {8, 8},
      {60, 8},
    }

    local distance = function(p_1, p_2)
      local dx = (p_1[1] or p_1.x) - (p_2[1] or p_2.x)
      local dy = (p_1[2] or p_1.y) - (p_2[2] or p_2.y)
      return ((dx * dx) + (dy * dy)) ^ 0.5
    end

    local direction = function(p_1, p_2)

      local d_x = (p_2[1] or p_2.x) - (p_1[1] or p_1.x)
      local d_y = (p_2[2] or p_2.y) - (p_1[2] or p_1.y)
      local angle = math.atan2(d_y, d_x)

      local orientation =  (angle / (2 * math.pi)) - 0.25
      if orientation < 0 then orientation = orientation + 1 end

      local direction = math.floor((orientation * 8) + 0.5)
      if direction == 8 then direction = 0 end
      return direction
    end

    local get_shoot_target = function(entity)
      local enemies = entity.surface.find_enemy_units(entity.position, 10)
      local closest = entity.surface.get_closest(entity.position, enemies)
      return closest
    end

    script.on_event(defines.events.on_tick, function()
      local k, destination = next(points)
      if not k then return end
      local target = {center[1] + destination[1], center[2] + destination[2]}
      if distance(character.position, target) < 1 then
        points[k] = nil
        return
      end

      if game.tick % 17 == 0 then
        local walking_direction = direction(target, character.position)
        character.walking_state = {walking = true, direction = walking_direction}
      end

      if not (shoot_target and shoot_target.valid) then
        shoot_target = get_shoot_target(character)
      end

      if shoot_target then
        character.shooting_state = {state = defines.shooting.shooting_enemies, position = shoot_target.position}
      else
        character.shooting_state = {state = defines.shooting.not_shooting}
      end

    end)

  ]]
}

menu_simulations.artillery =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-artillery.zip",
  length = 60 * 22,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.train_junction =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-train-junction.zip",
  length = 60 * 10,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.oil_pumpjacks =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-oil-pumpjacks.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.oil_refinery =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-oil-refinery.zip",
  length = 60 * 20,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.early_smelting =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-early-smelting.zip",
  length = 60 * 10,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.train_station =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-train-station.zip",
  length = 60 * 16,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.logistic_robots =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-logistic-robots.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.nuclear_power =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-nuclear-power.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    game.camera_position = {logo.position.x, logo.position.y+9.75}
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
  ]],
  update =
  [[
  ]]
}

menu_simulations.chase_player =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-chase-player.zip",
  length = 60 * 16,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    local center = {logo.position.x, logo.position.y+9.75}
    game.camera_position = center
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 0
    game.map_settings.steering.moving.force_unit_fuzzy_goto_behavior = true
    game.map_settings.steering.moving.radius = 1

    local character = game.surfaces[1].create_entity{name = "character", position = {center[1] - 55, center[2] + 4.5}, force = "player"}
    character.walking_state = {walking = true, direction = 2}
    character.character_running_speed_modifier = 0.2
    character.tick_of_last_attack = game.tick

    local biter = game.surfaces[1].create_entity{name = "small-biter", position = {center[1] - 40, center[2] + 4.5}}
    biter.speed = character.character_running_speed
    biter.set_command{type = defines.command.go_to_location, destination = {center[1] + 60, center[2] + 4.5}, distraction = defines.distraction.none}

    script.on_nth_tick(10, function()
      if biter.position.x < (center[1] + 50) then return end
      character.walking_state = {walking = true, direction = 6}
      character.tick_of_last_attack = 0
      character.character_running_speed_modifier = 0.6
      local command = {type = defines.command.go_to_location, destination_entity = character, distraction = defines.distraction.none}
      biter.set_command(command)
      biter.speed = character.character_running_speed
      local position = biter.position
      local surface = game.surfaces[1]
      local names = {"medium-biter", "small-biter", "small-biter", "small-biter"}
      for k = 1, 25 do
        local spawn_position = {position.x + math.random(-5, 5), position.y + math.random(-10, 10)}
        local name = names[math.random(#names)]
        local biter = surface.create_entity{name = name, position = position}
        biter.set_command(command)
        biter.speed = character.character_running_speed
      end
      script.on_nth_tick(10, nil)
    end)
  ]]
}

menu_simulations.big_defense =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-big-defense.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    logo.destructible = false
    local center = {logo.position.x, logo.position.y+9.75}
    game.camera_position = center
    game.camera_zoom = 1
    game.tick_paused = false
    game.surfaces.nauvis.daytime = 1
    game.map_settings.steering.moving.force_unit_fuzzy_goto_behavior = true
    game.map_settings.steering.moving.radius = 3

    local bop = function()
      local surface = game.surfaces[1]
      local target = surface.find_entities_filtered{name = "flamethrower-turret", position = {33.5, -12}}[1]
      local names = {"medium-biter", "medium-biter", "big-biter", "big-biter", "big-spitter", "medium-spitter"}
      for k = 1, 100 do
        local spawn_position = {center[1] - 60 + math.random(-35, 5), center[2] + math.random(-10, 10)}
        local name = names[math.random(#names)]
        local biter = surface.create_entity{name = name, position = spawn_position}
        biter.set_command({type = defines.command.attack, target = target})
        biter.speed = 0.24 + (math.random() / 20)
      end
    end

    bop()
  ]]
}

menu_simulations.brutal_defeat =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-brutal-defeat.zip",
  length = 60 * 18,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    logo.destructible = false
    local center = {logo.position.x, logo.position.y+9.75}
    game.camera_position = center
    game.camera_zoom = 1
    game.tick_paused = false
    game.map_settings.steering.moving.force_unit_fuzzy_goto_behavior = true
    game.map_settings.steering.moving.radius = 2

    game.forces.enemy.set_ammo_damage_modifier("melee", 10)
    game.forces.enemy.set_ammo_damage_modifier("biological", 10)
    game.forces.enemy.set_gun_speed_modifier("melee", 0.5)
    game.forces.enemy.set_gun_speed_modifier("biological", 0.5)


    local bop = function()
      local surface = game.surfaces[1]
      local targets = surface.find_entities_filtered{force = "player", position = {center[1] + 25, center[2]}, radius = 10}
      local count = #targets
      local names = {"medium-biter", "small-biter", "small-biter", "small-biter", "small-biter", "small-biter", "small-spitter"}
      for k = 1, 350 do
        local spawn_position = {center[1] - 40 + math.random(-55, 5), center[2] + 10 + math.random(-5, 5)}
        local name = names[math.random(#names)]
        local biter = surface.create_entity{name = name, position = spawn_position}
        biter.set_command
        {
          type = defines.command.compound,
          structure_type = defines.compound_command.return_last,
          commands =
          {
            {type = defines.command.attack, target = targets[math.random(count)]},
            {type = defines.command.attack_area, destination = {center[1] + 20, center[2]}, radius = math.random(5, 10)},
            {type = defines.command.attack_area, destination = {center[1] + 35, center[2]}, radius = math.random(2, 5)},
            {type = defines.command.go_to_location, destination = {center[1] + 120, center[2]}}
          }
        }
        biter.speed = 0.24 + (math.random() / 20)
      end
    end

    bop()
  ]]
}

menu_simulations.spider_ponds =
{
  checkboard = false,
  save = "__base__/menu-simulations/menu-simulation-spider-ponds.zip",
  length = 60 * 12,
  init =
  [[
    local logo = game.surfaces.nauvis.find_entities_filtered{name = "factorio-logo-11tiles", limit = 1}[1]
    logo.destructible = false
    local center = {logo.position.x, logo.position.y+9.75}
    game.camera_position = center
    game.camera_zoom = 1
    game.tick_paused = false

    local spider = game.surfaces.nauvis.find_entities_filtered{name = "spidertron", limit = 1}[1]

    points =
    {
      {-16, -8},
      {0, -12},
      {16, -8},
      {16, 0},
      {60, 60},
    }

    local bonk = function()
      local k, position = next(points)
      if not k then return end
      points[k] = nil
      local x = position[1] + center[1]
      local y = position[2] + center[2]
      spider.autopilot_destination = {x, y}
    end

    bonk()

    script.on_event(defines.events.on_spider_command_completed, function(event)
      bonk()
    end)

  ]]
}



return menu_simulations