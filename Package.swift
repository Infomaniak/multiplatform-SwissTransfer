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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.3/STCore.xcframework.zip",
            checksum: "dfbb88e21d0930eab098fa1c686b44d95a3d19bca3f95c3d33b7132ea604c2a7"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.3/STDatabase.xcframework.zip",
            checksum: "b869234d800d0ac1a98e080b02129f46df127a0908f7f56a556afa9696ecee34"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.3/STNetwork.xcframework.zip",
            checksum: "49c217eb225a12e49af2fd07255444fc38c81f144dd1a109684937c31ebc483c"
        ),
    ]
)
