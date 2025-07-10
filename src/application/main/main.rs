use dioxus::prelude::*;

// use crate::components::ModsAndPacks;
// use crate::components::Settings;
// use crate::components::Cloud;
// use crate::components::Home;

#[component]
pub fn Main() -> Element {
    let mut active_tab = use_signal(|| "Main");
    const LOGO: Asset = asset!("/public/assets/images/other/logo.png");
    const HOME: Asset = asset!("/public/assets/images/buttons/home.png");
    const MODS_AND_PACKS: Asset = asset!("/public/assets/images/buttons/mods_and_packs.png");
    const SETTINGS: Asset = asset!("/public/assets/images/buttons/settings.png");
    const CLOUD: Asset = asset!("/public/assets/images/buttons/cloud.png");
    const PLUS: Asset = asset!("/public/assets/images/buttons/plus.png");

    rsx! {
        style {
            dangerous_inner_html: include_str!("/Users/cubelius/RustroverProjects/Launcher/DreamLauncher/public/assets/styles/style_main.css")
        }

        div { class: "desktop",
            nav { class: "navigation",
                div { class: "logo-wrapper",
                    div { class: "logo",
                        img { src: "{LOGO}", alt: "Logo", class: "logo-img" }
                    }
                    h1 { class: "app-name", "Dream Launcher" }
                }

                ul { class: "nav-items",
                    li {
                        // (*active_tab)()
                        // active_tab: &active_tab ...
                        class: if (*active_tab)() == "Main" { "nav-item active" } else { "nav-item" },
                        onclick: move |_| active_tab.set("Main"),
                        img { class: "nav-icon", src: "{HOME}", alt: "Home" }
                        span { class: "nav-text", "Home" }
                    }
                    li {
                        class: if (*active_tab)() == "ModsAndPacks" { "nav-item active" } else { "nav-item" },
                        onclick: move |_| active_tab.set("ModsAndPacks"),
                        img { class: "nav-icon", src: "{MODS_AND_PACKS}", alt: "Mods & Packs" }
                        span { class: "nav-text", "Mods & Packs" }
                    }
                    li {
                        class: if (*active_tab)() == "Settings" { "nav-item active" } else { "nav-item" },
                        onclick: move |_| active_tab.set("Settings"),
                        img { class: "nav-icon", src: "{SETTINGS}", alt: "Settings" }
                        span { class: "nav-text", "Settings" }
                    }
                    li {
                        class: if (*active_tab)() == "Cloud" { "nav-item active" } else { "nav-item" },
                        onclick: move |_| active_tab.set("Cloud"),
                        img { class: "nav-icon", src: "{CLOUD}", alt: "Cloud" }
                        span { class: "nav-text", "Cloud" }
                    }
                    li {
                        class: if (*active_tab)() == "New" { "nav-item active" } else { "nav-item" },
                        onclick: move |_| active_tab.set("New"),
                        img { class: "nav-icon", src: "{PLUS}", alt: "New tab" }
                    }
                }
            }

            // Main content area
            main { class: "content",
                match (*active_tab)() {
                    "Main" => rsx! {
                        div { class: "main-content",
                            h1 { "Welcome to Dream Launcher" }
                            p { "This is the main page." }
                        }
                    },
                    "ModsAndPacks" => rsx! {
                        // ModsAndPacks {}
                        div { class: "main-content",
                            h1 { "Mods and Packs" }
                            p { "This is the Mods and Packs section." }
                        }
                    },
                    "Settings" => rsx! {
                        // Settings {}
                        div { class: "main-content",
                            h1 { "Settings" }
                            p { "This is the Settings section." }
                        }
                    },
                    "Cloud" => rsx! {
                        // Cloud {}
                        div { class: "main-content",
                            h1 { "Cloud" }
                            p { "This is the Cloud section." }
                        }
                    },
                    "New" => rsx! {
                        div { class: "main-content",
                            h1 { "New Tab" }
                            p { "This is a new tab." }
                        }
                    },
                    _ => rsx! {
                        div { class: "main-content",
                            h1 { "404" }
                            p { "Page not found." }
                        }
                    }
                }
            }
        }
    }
}