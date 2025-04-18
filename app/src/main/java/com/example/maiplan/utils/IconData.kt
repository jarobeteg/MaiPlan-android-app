package com.example.maiplan.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A centralized object that provides access to a set of [ImageVector] icons
 * categorized into groups like work, personal, health, finance, education, hobbies, and more.
 *
 * ## Overview
 * - [allIcons] is a Map<String, ImageVector> where each key is a human-readable string
 *   representing an icon name (search, home, etc.) and the value is the corresponding [ImageVector].
 * - [getIconByKey] provides a safe way to fetch an icon by its key, returning a fallback icon if the key is not found.
 *
 * ## Categories Included
 * - General and Miscellaneous
 * - Work
 * - Personal
 * - Health and Fitness
 * - Finance
 * - Shopping
 * - Education and Learning
 * - Family and Relationships
 * - Hobbies and Leisure
 * - Home and Maintenance
 * - Travel and Transportation
 * - Goals and Productivity
 */
object IconData {
    val allIcons: Map<String, ImageVector> = mapOf(
        // general and misc
        "search" to Icons.Filled.Search,
        "home" to Icons.Filled.Home,
        "settings" to Icons.Filled.Settings,
        "info" to Icons.Filled.Info,
        "help" to Icons.AutoMirrored.Filled.Help,
        "print" to Icons.Filled.Print,
        "feedback" to Icons.Filled.Feedback,
        "cached" to Icons.Filled.Cached,
        "perm media" to Icons.Filled.PermMedia,
        "token" to Icons.Filled.Token,
        "dangerous" to Icons.Filled.Dangerous,
        "link" to Icons.Filled.Link,
        "mail" to Icons.Filled.Mail,
        "calculate" to Icons.Filled.Calculate,
        "archive" to Icons.Filled.Archive,
        "call" to Icons.Filled.Call,
        "qr code" to Icons.Filled.QrCode,
        "cloud" to Icons.Filled.Cloud,
        "light mode" to Icons.Filled.LightMode,
        "dark mode" to Icons.Filled.DarkMode,

        // work
        "description" to Icons.Filled.Description,
        "dashboard" to Icons.Filled.Dashboard,
        "work" to Icons.Filled.Work,
        "article" to Icons.AutoMirrored.Filled.Article,
        "assignment" to Icons.AutoMirrored.Filled.Assignment,
        "assessment" to Icons.Filled.Assessment,
        "analytics" to Icons.Filled.Analytics,
        "view in ar" to Icons.Filled.ViewInAr,
        "dns" to Icons.Filled.Dns,
        "translate" to Icons.Filled.Translate,
        "source" to Icons.Filled.Source,
        "payments" to Icons.Filled.Payments,
        "inventory" to Icons.Filled.Inventory,
        "warehouse" to Icons.Filled.Warehouse,
        "local shipping" to Icons.Filled.LocalShipping,

        // personal
        "pets" to Icons.Filled.Pets,
        "redeem" to Icons.Filled.Redeem,
        "offline bolt" to Icons.Filled.OfflineBolt,
        "offline pin" to Icons.Filled.OfflinePin,
        "view agenda" to Icons.Filled.ViewAgenda,
        "hourglass full" to Icons.Filled.HourglassFull,
        "cake" to Icons.Filled.Cake,
        "recycling" to Icons.Filled.Recycling,
        "interests" to Icons.Filled.Interests,
        "compost" to Icons.Filled.Compost,
        "emoji nature" to Icons.Filled.EmojiNature,
        "contact mail" to Icons.Filled.ContactMail,
        "alternate email" to Icons.Filled.AlternateEmail,
        "key" to Icons.Filled.Key,
        "contacts" to Icons.Filled.Contacts,
        "message" to Icons.AutoMirrored.Filled.Message,
        "local gas station" to Icons.Filled.LocalGasStation,
        "spa" to Icons.Filled.Spa,
        "grass" to Icons.Filled.Grass,
        "yard" to Icons.Filled.Yard,

        // health and fitness
        "health and safety" to Icons.Filled.HealthAndSafety,
        "healing" to Icons.Filled.Healing,
        "water drop" to Icons.Filled.WaterDrop,
        "vaccines" to Icons.Filled.Vaccines,
        "sick" to Icons.Filled.Sick,
        "sports basketball" to Icons.Filled.SportsBasketball,
        "sports tennis" to Icons.Filled.SportsTennis,
        "sports volleyball" to Icons.Filled.SportsVolleyball,
        "sports baseball" to Icons.Filled.SportsBaseball,
        "sports football" to Icons.Filled.SportsFootball,
        "clean hands" to Icons.Filled.CleanHands,
        "sanitizer" to Icons.Filled.Sanitizer,
        "local hospital" to Icons.Filled.LocalHospital,
        "local florist" to Icons.Filled.LocalFlorist,
        "fitness center" to Icons.Filled.FitnessCenter,

        // finance
        "paid" to Icons.Filled.Paid,
        "credit card" to Icons.Filled.CreditCard,
        "account balance" to Icons.Filled.AccountBalance,
        "account balance wallet" to Icons.Filled.AccountBalanceWallet,
        "savings" to Icons.Filled.Savings,
        "currency exchange" to Icons.Filled.CurrencyExchange,
        "balance" to Icons.Filled.Balance,
        "request page" to Icons.Filled.RequestPage,
        "wallet" to Icons.Filled.Wallet,
        "electrical services" to Icons.Filled.ElectricalServices,

        // shopping
        "shopping cart" to Icons.Filled.ShoppingCart,
        "shopping bag" to Icons.Filled.ShoppingBag,
        "shopping basket" to Icons.Filled.ShoppingBasket,
        "receipt" to Icons.Filled.Receipt,
        "store" to Icons.Filled.Store,
        "shop" to Icons.Filled.Shop,
        "contactless" to Icons.Filled.Contactless,

        // education and learning
        "find in page" to Icons.Filled.FindInPage,
        "terminal" to Icons.Filled.Terminal,
        "school" to Icons.Filled.School,
        "psychology" to Icons.Filled.Psychology,
        "science" to Icons.Filled.Science,
        "architecture" to Icons.Filled.Architecture,
        "content cut" to Icons.Filled.ContentCut,
        "create" to Icons.Filled.Create,

        // family and relationship
        "favorite" to Icons.Filled.Favorite,
        "loyalty" to Icons.Filled.Loyalty,
        "person" to Icons.Filled.Person,
        "groups" to Icons.Filled.Groups,
        "people" to Icons.Filled.People,

        // hobbies and leisure
        "extension" to Icons.Filled.Extension,
        "important devices" to Icons.Filled.ImportantDevices,
        "theaters" to Icons.Filled.Theaters,
        "movie" to Icons.Filled.Movie,
        "mic" to Icons.Filled.Mic,
        "emoji emotions" to Icons.Filled.EmojiEmotions,
        "sports esports" to Icons.Filled.SportsEsports,
        "emoji food beverage" to Icons.Filled.EmojiFoodBeverage,
        "outdoor grill" to Icons.Filled.OutdoorGrill,
        "fireplace" to Icons.Filled.Fireplace,
        "weekend" to Icons.Filled.Weekend,
        "photo camera" to Icons.Filled.PhotoCamera,
        "videocam" to Icons.Filled.Videocam,
        "image" to Icons.Filled.Image,
        "palette" to Icons.Filled.Palette,
        "audio track" to Icons.Filled.Audiotrack,
        "restaurant" to Icons.Filled.Restaurant,
        "fast food" to Icons.Filled.Fastfood,
        "lunch dining" to Icons.Filled.LunchDining,
        "local cafe" to Icons.Filled.LocalCafe,
        "liquor" to Icons.Filled.Liquor,
        "icecream" to Icons.Filled.Icecream,
        "smartphone" to Icons.Filled.Smartphone,
        "laptop" to Icons.Filled.Laptop,
        "tv" to Icons.Filled.Tv,
        "desktop windows" to Icons.Filled.DesktopWindows,
        "speaker" to Icons.Filled.Speaker,
        "coffee maker" to Icons.Filled.CoffeeMaker,

        // home and maintenance
        "build" to Icons.Filled.Build,
        "event seat" to Icons.Filled.EventSeat,
        "engineering" to Icons.Filled.Engineering,
        "construction" to Icons.Filled.Construction,
        "handyman" to Icons.Filled.Handyman,
        "king bed" to Icons.Filled.KingBed,
        "home repair service" to Icons.Filled.HomeRepairService,
        "local laundry service" to Icons.Filled.LocalLaundryService,
        "dry cleaning" to Icons.Filled.DryCleaning,
        "car repair" to Icons.Filled.CarRepair,

        // travel and transportation
        "explore" to Icons.Filled.Explore,
        "flight" to Icons.Filled.Flight,
        "commute" to Icons.Filled.Commute,
        "tour" to Icons.Filled.Tour,
        "anchor" to Icons.Filled.Anchor,
        "location city" to Icons.Filled.LocationCity,
        "local car wash" to Icons.Filled.LocalCarWash,
        "luggage" to Icons.Filled.Luggage,
        "hiking" to Icons.Filled.Hiking,
        "wb sunny" to Icons.Filled.WbSunny,
        "wb cloudy" to Icons.Filled.WbCloudy,
        "wb twilight" to Icons.Filled.WbTwilight,
        "landscape" to Icons.Filled.Landscape,
        "map" to Icons.Filled.Map,
        "directions car" to Icons.Filled.DirectionsCar,
        "directions bus" to Icons.Filled.DirectionsBus,
        "directions boat" to Icons.Filled.DirectionsBoat,
        "park" to Icons.Filled.Park,
        "forest" to Icons.Filled.Forest,
        "hotel" to Icons.Filled.Hotel,
        "traffic" to Icons.Filled.Traffic,
        "subway" to Icons.Filled.Subway,

        // goals and productivity
        "lightbulb" to Icons.Filled.Lightbulb,
        "tips and updates" to Icons.Filled.TipsAndUpdates,
        "timeline" to Icons.Filled.Timeline,
        "rocket launch" to Icons.Filled.RocketLaunch,
        "grade" to Icons.Filled.Grade,
    )

    /**
     * Retrieves an [ImageVector] based on the provided [key].
     *
     * @param key The string key representing the desired icon (home, flight, shopping cart, etc.).
     * @return The matching [ImageVector] if the key exists, or a fallback Help icon if the key is not found.
     */
    fun getIconByKey(key: String): ImageVector {
        return allIcons[key] ?: Icons.AutoMirrored.Filled.Help
    }
}