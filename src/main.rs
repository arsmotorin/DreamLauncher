use dioxus::prelude::*;
use dioxus_desktop::{Config, LogicalSize, WindowBuilder};
mod creeper;
mod application;
mod play_together;
mod chats;
mod cloud;

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
        .launch(application::auth::auth::app);
}

fn call_creeper() {
    // let _ = creeper::creeper::main();
}