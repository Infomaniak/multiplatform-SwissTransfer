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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/2.0.0/STCore.xcframework.zip",
            checksum: "701fe40df81ba7bf4b1ff77c6846d0f2ecf2997a7e8692ae6e7a77d4051d2d9f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/2.0.0/STDatabase.xcframework.zip",
            checksum: "0c9730fe9bf0017d3347a88e79b61a976753b93450e3067eb14d20b65fd75de7"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/2.0.0/STNetwork.xcframework.zip",
            checksum: "8f2dfc9fbb74c739cca1ac38a8f6f9caea883c0d48673c5186a68d5f99359d28"
        ),
    ]
)
