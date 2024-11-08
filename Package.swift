// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.2/STCore.xcframework.zip",
            checksum: "cf650da9ab82f64a16f08fadd41bc1f1f0cc9ac29f8a855a333501e5c0e5d5d6"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.2/STDatabase.xcframework.zip",
            checksum: "038e471b116c17e03c1484c20f7cbd4aaf832c1a30389aecf17e28f05c4fabd4"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.2/STNetwork.xcframework.zip",
            checksum: "63213a80a7954649ea81cd12cb95c68e87516f43debbb75f9c37932bc1450f68"
        ),
    ]
)
