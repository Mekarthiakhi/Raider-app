-- ============================================================================
-- 004_seed_remote_selectors.sql
-- Fleet Ride Tracker — Initial Remote Selector Configurations
--
-- Remote selectors allow the companion app's AccessibilityService to parse
-- ride offer, accepted, in-trip, and completed screens without requiring
-- an app update whenever Rapido, Uber, or Ola push UI changes.
--
-- NOTE: View IDs and patterns are seeded with common production IDs and
-- robust regex fallbacks (multi-language English, Hindi, Telugu).
-- These can be live-updated in Supabase and pushed via Realtime.
-- ============================================================================

INSERT INTO public.remote_selectors (
    platform,
    app_version,
    screen_type,
    selectors,
    priority,
    active
) VALUES
-- ============================================================================
-- 1. RAPIDO CAPTAIN (Package: com.rapido.rider)
-- ============================================================================
(
    'rapido',
    '*',
    'completed',
    '{
        "platform": "rapido",
        "screen": "completed",
        "description": "Rapido ride completion screen showing final fare and distance",
        "fields": {
            "fare": {
                "view_ids": [
                    "com.rapido.rider:id/tv_fare_amount",
                    "com.rapido.rider:id/tv_total_fare",
                    "com.rapido.rider:id/txt_fare",
                    "com.rapido.rider:id/fare_text",
                    "com.rapido.rider:id/amount_tv"
                ],
                "regex_patterns": [
                    "(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
                    "([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:₹|Rs\\.?)"
                ],
                "keywords": {
                    "en": ["Fare", "Total Fare", "Collect Cash", "Earnings", "Trip Fare", "Total"],
                    "hi": ["किराया", "कुल किराया", "कैश लें", "कमाई"],
                    "te": ["ఛార్జీ", "మొత్తం ఛార్జీ", "నగదు తీసుకోండి"]
                }
            },
            "distance_km": {
                "view_ids": [
                    "com.rapido.rider:id/tv_trip_distance",
                    "com.rapido.rider:id/tv_distance",
                    "com.rapido.rider:id/txt_distance",
                    "com.rapido.rider:id/distance_text"
                ],
                "regex_patterns": [
                    "([0-9]+(?:\\.[0-9]+)?)\\s*(?:km|kms|KM|KMS|కిమీ|किमी)",
                    "(?:Distance|दूरी|దూరం)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)"
                ],
                "keywords": {
                    "en": ["km", "kms", "Distance", "Trip Distance"],
                    "hi": ["किमी", "दूरी"],
                    "te": ["కిమీ", "దూరం"]
                }
            },
            "duration_min": {
                "view_ids": [
                    "com.rapido.rider:id/tv_trip_duration",
                    "com.rapido.rider:id/tv_duration",
                    "com.rapido.rider:id/txt_time"
                ],
                "regex_patterns": [
                    "([0-9]+)\\s*(?:min|mins|MIN|MINS|मिनट|నిమిషాలు)",
                    "([0-9]+)\\s*hr\\s*([0-9]+)\\s*min"
                ],
                "keywords": {
                    "en": ["min", "mins", "Duration", "Trip Time"],
                    "hi": ["मिनट", "समय"],
                    "te": ["నిమిషాలు", "సమయం"]
                }
            },
            "pickup": {
                "view_ids": [
                    "com.rapido.rider:id/tv_pickup_location",
                    "com.rapido.rider:id/pickup_address_text"
                ]
            },
            "dropoff": {
                "view_ids": [
                    "com.rapido.rider:id/tv_drop_location",
                    "com.rapido.rider:id/drop_address_text"
                ]
            }
        }
    }'::jsonb,
    100,
    true
),
(
    'rapido',
    '*',
    'accepted',
    '{
        "platform": "rapido",
        "screen": "accepted",
        "description": "Rapido ride accepted state (pickup navigation)",
        "fields": {
            "pickup": {
                "view_ids": [
                    "com.rapido.rider:id/tv_pickup_address",
                    "com.rapido.rider:id/pickup_location_text"
                ]
            },
            "customer_name": {
                "view_ids": [
                    "com.rapido.rider:id/tv_customer_name",
                    "com.rapido.rider:id/rider_name"
                ]
            }
        },
        "trigger_keywords": {
            "en": ["Navigate to Pickup", "Arrived at Pickup", "Start Ride", "Pickup Customer", "OTP"],
            "hi": ["पिकअप पर जाएं", "पिकअप पर पहुंचे", "राइड शुरू करें", "ओटीपी"],
            "te": ["పికప్ వద్దకు వెళ్ళండి", "రైడ్ ప్రారంభించండి"]
        }
    }'::jsonb,
    100,
    true
),

-- ============================================================================
-- 2. UBER DRIVER (Package: com.ubercab.driver)
-- ============================================================================
(
    'uber',
    '*',
    'completed',
    '{
        "platform": "uber",
        "screen": "completed",
        "description": "Uber Driver trip completion / receipt screen",
        "fields": {
            "fare": {
                "view_ids": [
                    "com.ubercab.driver:id/ub__fare_total_amount",
                    "com.ubercab.driver:id/trip_receipt_fare",
                    "com.ubercab.driver:id/ub__payment_summary_amount",
                    "com.ubercab.driver:id/header_value"
                ],
                "regex_patterns": [
                    "(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
                    "([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:₹|Rs\\.?)"
                ],
                "keywords": {
                    "en": ["Trip Balance", "Total Payout", "Your Earnings", "Fare", "Cash collected"],
                    "hi": ["किराया", "आपकी कमाई", "कैश मिला"],
                    "te": ["మీ సంపాదన", "ఛార్జీ"]
                }
            },
            "distance_km": {
                "view_ids": [
                    "com.ubercab.driver:id/ub__trip_distance",
                    "com.ubercab.driver:id/distance_value"
                ],
                "regex_patterns": [
                    "([0-9]+(?:\\.[0-9]+)?)\\s*(?:km|kms|KM)",
                    "([0-9]+(?:\\.[0-9]+)?)\\s*(?:mi|miles)"
                ],
                "keywords": {
                    "en": ["km", "kms", "Distance"],
                    "hi": ["किमी", "दूरी"],
                    "te": ["కిమీ"]
                }
            },
            "duration_min": {
                "view_ids": [
                    "com.ubercab.driver:id/ub__trip_time",
                    "com.ubercab.driver:id/duration_value"
                ],
                "regex_patterns": [
                    "([0-9]+)\\s*(?:min|mins|MIN)",
                    "([0-9]+)\\s*hr\\s*([0-9]+)\\s*m"
                ],
                "keywords": {
                    "en": ["min", "mins", "Duration"],
                    "hi": ["मिनट"],
                    "te": ["నిమిషాలు"]
                }
            }
        }
    }'::jsonb,
    100,
    true
),
(
    'uber',
    '*',
    'accepted',
    '{
        "platform": "uber",
        "screen": "accepted",
        "description": "Uber Driver accepted ride navigation screen",
        "fields": {
            "pickup": {
                "view_ids": [
                    "com.ubercab.driver:id/ub__pickup_address",
                    "com.ubercab.driver:id/address_text"
                ]
            }
        },
        "trigger_keywords": {
            "en": ["Navigate", "Arrive", "Pickup", "Start Trip", "Slide to Start", "UberGo", "UberPremier"],
            "hi": ["नेविगेट करें", "पहुंच गए", "ट्रिप शुरू करें"],
            "te": ["నేవిగేట్ చేయండి", "ట్రిప్ ప్రారంభించండి"]
        }
    }'::jsonb,
    100,
    true
),

-- ============================================================================
-- 3. OLA DRIVER (Package: com.olacabs.oladriver)
-- ============================================================================
(
    'ola',
    '*',
    'completed',
    '{
        "platform": "ola",
        "screen": "completed",
        "description": "Ola Driver bill / trip summary screen",
        "fields": {
            "fare": {
                "view_ids": [
                    "com.olacabs.oladriver:id/tv_bill_amount",
                    "com.olacabs.oladriver:id/tv_ride_fare",
                    "com.olacabs.oladriver:id/tv_cash_to_collect",
                    "com.olacabs.oladriver:id/fare_amount"
                ],
                "regex_patterns": [
                    "(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
                    "([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:₹|Rs\\.?)"
                ],
                "keywords": {
                    "en": ["Total Bill", "Ride Fare", "Cash to Collect", "Online Payment", "Ola Money"],
                    "hi": ["कुल बिल", "किराया", "नकद लें"],
                    "te": ["మొత్తం బిల్లు", "రైడ్ ఛార్జీ"]
                }
            },
            "distance_km": {
                "view_ids": [
                    "com.olacabs.oladriver:id/tv_total_km",
                    "com.olacabs.oladriver:id/tv_distance",
                    "com.olacabs.oladriver:id/distance_txt"
                ],
                "regex_patterns": [
                    "([0-9]+(?:\\.[0-9]+)?)\\s*(?:km|kms|KM)",
                    "(?:Distance|दूरी)\\s*[:\\-]?\\s*([0-9]+(?:\\.[0-9]+)?)"
                ],
                "keywords": {
                    "en": ["km", "kms", "Distance", "Total km"],
                    "hi": ["किमी", "कुल किमी"],
                    "te": ["కిమీ"]
                }
            },
            "duration_min": {
                "view_ids": [
                    "com.olacabs.oladriver:id/tv_total_time",
                    "com.olacabs.oladriver:id/tv_duration"
                ],
                "regex_patterns": [
                    "([0-9]+)\\s*(?:min|mins|MIN)",
                    "([0-9]+)\\s*hr\\s*([0-9]+)\\s*m"
                ],
                "keywords": {
                    "en": ["min", "mins", "Time", "Ride Time"],
                    "hi": ["मिनट", "समय"],
                    "te": ["నిమిషాలు"]
                }
            }
        }
    }'::jsonb,
    100,
    true
),
(
    'ola',
    '*',
    'accepted',
    '{
        "platform": "ola",
        "screen": "accepted",
        "description": "Ola Driver accepted ride screen",
        "fields": {
            "pickup": {
                "view_ids": [
                    "com.olacabs.oladriver:id/tv_pickup_address",
                    "com.olacabs.oladriver:id/pickup_loc"
                ]
            }
        },
        "trigger_keywords": {
            "en": ["Go to Pickup", "Start Ride", "Enter OTP", "Arrived", "Client Pickup"],
            "hi": ["पिकअप पर जाएं", "राइड शुरू करें", "ओटीपी दर्ज करें"],
            "te": ["పికప్ వద్దకు వెళ్ళండి", "ఓటీపీ నమోదు చేయండి"]
        }
    }'::jsonb,
    100,
    true
);

COMMENT ON TABLE public.remote_selectors IS 'Initial seed data for Rapido, Uber, and Ola remote selectors across completed and accepted screens.';

