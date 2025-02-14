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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.6/STCore.xcframework.zip",
            checksum: "d1bdbec89e82ee5d06e93a1e6658992c966b33ae81983ff549a1d5dacb933fff"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.6/STDatabase.xcframework.zip",
            checksum: "253cf9cb48a85796f0d23d0d0191437cb58e9f850bf9a0ea6d3dae59c50633dd"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.6/STNetwork.xcframework.zip",
            checksum: "04c41feb95dd276ba132fb4afcd10a702ee80915fd671972cdbb852078a35d02"
        ),
    ]
)
