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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.2/STCore.xcframework.zip",
            checksum: "8eea08f09db3389b39ad99c6a260f948d92cd86ff251d194207def2a31fcf38f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.2/STDatabase.xcframework.zip",
            checksum: "8ae0ead235b8ea91e3e110379e4e6271079caa7f0424b35037a89472c794afbc"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.2/STNetwork.xcframework.zip",
            checksum: "f261e02729b7a7cb2b154bb650fd7673ff5825120655a444f5e51cbc2b903627"
        ),
    ]
)
