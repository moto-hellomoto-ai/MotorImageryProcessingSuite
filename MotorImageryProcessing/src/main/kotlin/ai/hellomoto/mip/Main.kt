package ai.hellomoto.mip

import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths


class Main : App(MainView::class, Styles::class) {
    override val configBasePath: Path = Paths.get(System.getProperty("user.home"), ".mip", "config")
}

fun main(args: Array<String>) {
    launch<Main>(args)
}