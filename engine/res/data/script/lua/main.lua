-- Einfache Schleife
for i = 1, 10 do
    -- Wenn-Bedingung
    if i % 2 == 0 then
        print("Gerade Zahl: " .. i)  -- String und String-Konkatenation
    else
        print("Ungerade Zahl: " .. i)
    end
end

-- Funktionsdefinition
function quadrat(x)
    return x * x  -- Multiplikation
end

-- Tabellen (Arrays)
local zahlen = {1, 2, 3, 4, 5}
for _, zahl in ipairs(zahlen) do
    print("Quadrat von " .. zahl .. " ist " .. quadrat(zahl))
end

-- Mehrzeiliger Kommentar
--[[
Dies ist ein mehrzeiliger Kommentar.
Er wird verwendet, um längere Texte oder Codebeschreibungen einzufügen.
]]

-- Einzelzeiliger Kommentar
-- Dies ist ein einzelner Kommentar.


manager:setX(5.0)
manager:setY(-3.0)
manager:setZ(8.0)
manager:setName("Test - Lua")
test = manager:getTest()
test:setName("Object from Lua")
test:mix()