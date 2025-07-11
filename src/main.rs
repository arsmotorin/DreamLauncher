use dioxus::prelude::*;
use dioxus_desktop::{Config, LogicalSize, WindowBuilder};
use dioxus_router::prelude::*;
mod creeper;
mod application;
mod play_together;
mod chats;

use crate::application::auth::auth::Auth;
use crate::application::auth::auth_context::AuthState;
use crate::application::main::main::Main;
use crate::application::main::home::Home;
use crate::application::main::mods_and_packs::ModsAndPacks;
use crate::application::main::settings::Settings;
use crate::application::main::cloud::Cloud;
use crate::application::main::new::New;

#[derive(Clone, Routable, Debug, PartialEq)]
pub enum Route {
    #[route("/auth")]
    Auth {},
    #[layout(Main)]
    #[redirect("/", || Route::Home {})]
    #[route("/home")]
    Home {},
    #[route("/mods_and_packs")]
    ModsAndPacks {},
    #[route("/settings")]
    Settings {},
    #[route("/cloud")]
    Cloud {},
    #[route("/new")]
    New {},
}

#[component]
fn Root() -> Element {
    let is_authenticated = use_signal(|| false);

    provide_context(AuthState { is_authenticated: is_authenticated.clone() });

    rsx! {
        Router::<Route> {}
    }
}

pub fn main() {
    let size = LogicalSize::new(1280.0, 832.0);

    let config = Config::default().with_window(
        WindowBuilder::new()
            .with_title("Dream Launcher")
            .with_inner_size(size)
            .with_min_inner_size(size)
            .with_max_inner_size(size)
            .with_resizable(false),
    );

    LaunchBuilder::new()
        .with_cfg(config)
        .launch(Root);
}